using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Lab4
{
    public static class TaskMechanism
    {
        private static List<string> HOSTS;

        public static void run(List<string> hosts)
        {
            HOSTS = hosts;

            var tasks = new List<Task>();

            for(var i = 0; i< HOSTS.Count; i++)
            {
                tasks.Add(Task.Factory.StartNew(start, i));
            }

            Task.WaitAll(tasks.ToArray());
        }

        public static void start(object objectID)
        {
            var id = (int)objectID;

            StartClient(HOSTS[id], id);
        }

        /*
         * Methos used to connect to the server
         */
        public static void StartClient(string host, int id)
        {
            //First we need to establish the remote endpoint to the server
            var hostIP = Dns.GetHostEntry(host.Split('/')[0]);
            var adressIP = hostIP.AddressList[0];
            var remoteEndPoint = new IPEndPoint(adressIP, HttpUtils.HTTP_PORT);

            //Create the TCP/IP socket
            var client = new Socket(adressIP.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            //Wrapper for storing the connection info
            var httpObject = new HttpObject
            {
                socket = client,
                hostname = host.Split('/')[0],
                endpoint = host.Contains("/") ? host.Substring(host.IndexOf("/")) : "/",
                remoteEndPoint = remoteEndPoint,
                id = id
            };

            //Connect to the remote endpoint  
            ConnectWrapper(httpObject).Wait();

            //Request data from the server
            SendWrapper(httpObject, HttpUtils.getRequestString(httpObject.hostname, httpObject.endpoint)).Wait();

            //Receive the response from the server
            ReceiveWrapper(httpObject).Wait();

            Console.WriteLine(
                "{0}) Response received : expected {1} chars in body, got {2} chars (headers + body)",
                id, HttpUtils.getContentLength(httpObject.responseContent.ToString()), httpObject.responseContent.Length);

            //Shutdown and close the socket
            client.Shutdown(SocketShutdown.Both);
            client.Close();
        }

        private static Task ConnectWrapper(HttpObject httpObj)
        {
            httpObj.socket.BeginConnect(httpObj.remoteEndPoint, ConnectCallback, httpObj);

            return Task.FromResult(httpObj.connectDone.WaitOne());
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

            //Signal that the connection took place
            httpObject.connectDone.Set();
        }

        private static Task SendWrapper(HttpObject httpObj, string data)
        {
            //Convert the string data to byte data using ASCII encoding
            var byteData = Encoding.ASCII.GetBytes(data);

            //Begin sending the data to the server  
            httpObj.socket.BeginSend(byteData, 0, byteData.Length, 0, SendCallback, httpObj);

            return Task.FromResult(httpObj.sendDone.WaitOne());
        }

        private static void SendCallback(IAsyncResult ar)
        {
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;
            var clientID = httpObject.id;

            //Complete sending the data to the server  
            var bytesSent = clientSocket.EndSend(ar);
            Console.WriteLine("{0} --> Sent {1} bytes to server.", clientID, bytesSent);

            //Signal that all bytes have been sent
            httpObject.sendDone.Set();
        }

        private static Task ReceiveWrapper(HttpObject httpObj)
        {
            //Begin receiving the data from the server
            httpObj.socket.BeginReceive(httpObj.buffer, 0, HttpObject.BUFFER_SIZE, 0, ReceiveCallback, httpObj);

            return Task.FromResult(httpObj.receiveDone.WaitOne());
        }

        private static void ReceiveCallback(IAsyncResult ar)
        {
            //Retrieve the details from the connection information wrapper
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;

            try
            {
                //Read data from the remote device
                var bytesRead = clientSocket.EndReceive(ar);

                //Get from the buffer a number of characters <= to the buffer size, and store it in the responseContent
                httpObject.responseContent.Append(Encoding.ASCII.GetString(httpObject.buffer, 0, bytesRead));

                //If the response header was not fully obtained get the next chunk of data
                if (!HttpUtils.getResponseHeader(httpObject.responseContent.ToString()))
                {
                    clientSocket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, ReceiveCallback, httpObject);
                }
                else
                {
                    //Response header fully obtained -> get the body
                    var responseBody = HttpUtils.getResponse(httpObject.responseContent.ToString());

                    //Custom header parser used to check if the data received so far has the length specified in the response headers
                    if (responseBody.Length < HttpUtils.getContentLength(httpObject.responseContent.ToString()))
                    {
                        //If it isn't, than more data is going to be retrieved
                        clientSocket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, ReceiveCallback, httpObject);
                    }
                    else
                    {
                        //Else, all the data has been received and signal that all bytes have been received  
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
