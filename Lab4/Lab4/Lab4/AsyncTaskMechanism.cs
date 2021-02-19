using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Lab4
{
    public static class AsyncTaskMechanism
    {
        public static List<string> HOSTS;


        public static void run(List<string> hosts)
        {
            HOSTS = hosts;

            var tasks = new List<Task>();

            for (var i = 0; i < HOSTS.Count; i++)
            {
                tasks.Add(Task.Factory.StartNew(start, i));
            }

            Task.WaitAll(tasks.ToArray());
        }

        private static void start(object objectID)
        {
            var id = (int)objectID;

            StartClient(HOSTS[id], id);
        }

        /**
         * Methos used to connect to a server.
         */
        private static async void StartClient(string host, int id)
        {
            //Establish the remote endpoint of the server  
            var hostIP = Dns.GetHostEntry(host.Split('/')[0]);
            var adressIP = hostIP.AddressList[0];
            var remoteEndpoint = new IPEndPoint(adressIP, HttpUtils.HTTP_PORT);

            //Create the TCP/IP socket
            var client = new Socket(adressIP.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            //Create a wrapper for the connection information
            var httpObject = new HttpObject
            {
                socket = client,
                hostname = host.Split('/')[0],
                endpoint = host.Contains("/") ? host.Substring(host.IndexOf("/")) : "/",
                remoteEndPoint = remoteEndpoint,
                id = id
            };

            //Connect to the remote endpoint  
            await ConnectWrapper(httpObject);

            //Request data from the server
            await SendWrapper(httpObject, HttpUtils.getRequestString(httpObject.hostname, httpObject.endpoint));

            //Receive the response from the server
            await ReceiveWrapper(httpObject);

            Console.WriteLine(
                "{0} --> Response received : expected {1} chars in body, got {2} chars (headers + body)",
                id, HttpUtils.getContentLength(httpObject.responseContent.ToString()), httpObject.responseContent.Length);

            //Shutdown and close the socket
            client.Shutdown(SocketShutdown.Both);
            client.Close();
        }

        private static async Task ConnectWrapper(HttpObject httpObj)
        {
            httpObj.socket.BeginConnect(httpObj.remoteEndPoint, ConnectCallback, httpObj);

            await Task.FromResult<object>(httpObj.connectDone.WaitOne());
        }

        private static void ConnectCallback(IAsyncResult ar)
        {
            //Retrieve the details from the connection information wrapper
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;
            var clientID = httpObject.id;
            var hostName = httpObject.hostname;

            //Complete the connection  
            clientSocket.EndConnect(ar);

            Console.WriteLine("{0} --> Socket connected to {1} ({2})", clientID, hostName, clientSocket.RemoteEndPoint);

            //Signal that the connection has been made 
            httpObject.connectDone.Set();
        }

        private static async Task SendWrapper(HttpObject httpObj, string data)
        {
            //Convert the string data to byte data using ASCII encoding.  
            var byteData = Encoding.ASCII.GetBytes(data);

            //Begin sending the data to the server  
            httpObj.socket.BeginSend(byteData, 0, byteData.Length, 0, SendCallback, httpObj);

            await Task.FromResult<object>(httpObj.sendDone.WaitOne());
        }

        private static void SendCallback(IAsyncResult ar)
        {
            var state = (HttpObject)ar.AsyncState;
            var clientSocket = state.socket;
            var clientID = state.id;

            //Complete sending the data to the server  
            var bytesSent = clientSocket.EndSend(ar);
            Console.WriteLine("{0} --> Sent {1} bytes to server.", clientID, bytesSent);

            //Signal that all bytes have been sent
            state.sendDone.Set();
        }

        private static async Task ReceiveWrapper(HttpObject httpObj)
        {
            //Begin receiving the data from the server
            httpObj.socket.BeginReceive(httpObj.buffer, 0, HttpObject.BUFFER_SIZE, 0, ReceiveCallback, httpObj);

            await Task.FromResult<object>(httpObj.receiveDone.WaitOne());
        }

        private static void ReceiveCallback(IAsyncResult ar)
        {
            //Retrieve the details from the connection information wrapper
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;

            try
            {
                //Read data from the remote device.  
                var bytesRead = clientSocket.EndReceive(ar);

                //Get from the buffer, a number of characters <= to the buffer size, and store it in the responseContent
                httpObject.responseContent.Append(Encoding.ASCII.GetString(httpObject.buffer, 0, bytesRead));

                //If the response header was not fully obtained, get the next chunk of data
                if (!HttpUtils.getResponseHeader(httpObject.responseContent.ToString()))
                {
                    clientSocket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, ReceiveCallback, httpObject);
                }
                else
                {
                    //Header was fully obtained -> get the body
                    var responseBody = HttpUtils.getResponse(httpObject.responseContent.ToString());

                    //Custom header parser is being used to check if the data received so far has the length specified in the response headers
                    if (responseBody.Length < HttpUtils.getContentLength(httpObject.responseContent.ToString()))
                    {
                        //If it isn't, than more data is to be retrieve
                        clientSocket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, ReceiveCallback, httpObject);
                    }
                    else
                    {
                        //Otherwise, all the data was received -> signal that all bytes have been received  
                        httpObject.receiveDone.Set();
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }

        }
    }
}