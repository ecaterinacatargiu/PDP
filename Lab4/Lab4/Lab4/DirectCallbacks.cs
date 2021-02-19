using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Lab4
{
    public static class DirectCallbacks
    {
        private static List<string> HOSTS;

        public static void run(List<string> hosts)
        {
            HOSTS = hosts;

            for(var i=0;i<HOSTS.Count;i++)
            {
                start(i);
                Thread.Sleep(5000);
            }
        }

        public static void start(object objectID)
        {
            var id = (int) objectID;

            startClient(HOSTS[id], id);
        }

        /*
         * Method used to connect to the server
         */
        public static void startClient(string host, int id)
        {
            //First we establish the remote endpoint of the server
            var hostIP = Dns.GetHostEntry(host.Split('/')[0]);
            var addressIP = hostIP.AddressList[0];
            var remoteEndPoint = new IPEndPoint(addressIP, HttpUtils.HTTP_PORT);

            //Then we create the TCP/IP socket
            var client = new Socket(addressIP.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

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
            httpObject.socket.BeginConnect(httpObject.remoteEndPoint, Connected, httpObject);

        }

        private static void Connected(IAsyncResult ar)
        {
            //Retrieve details from the connection info wrapper
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;
            var clientID = httpObject.id;
            var hostName = httpObject.hostname;

            clientSocket.EndConnect(ar);
            Console.WriteLine("{0} --> Socket connected to {1} ({2})", clientID, hostName, clientSocket.RemoteEndPoint);

            //Convert the string data to byte data using ASCII encoding.  
            var byteData = Encoding.ASCII.GetBytes(HttpUtils.getRequestString(httpObject.hostname, httpObject.endpoint));

            //Begin sending the data information to the server
            httpObject.socket.BeginSend(byteData, 0, byteData.Length, 0, Sent, httpObject);
        }


        private static void Sent(IAsyncResult ar)
        {
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;
            var clientID = httpObject.id;

            //Complete sending the data to the server
            var byteSent = clientSocket.EndSend(ar);
            Console.WriteLine("{0} --> Sent {1} bytes to server.", clientID, byteSent);

            //Retrieve the response from the server
            httpObject.socket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, Receiving, httpObject);
        }

        private static void Receiving(IAsyncResult ar)
        {
            //Retrieve detailes from the connection info wrapper
            var httpObject = (HttpObject)ar.AsyncState;
            var clientSocket = httpObject.socket;
            var clientID = httpObject.id;

            try
            {
                //Read all the data from the remote device
                var byteRead = clientSocket.EndReceive(ar);

                //Get from the buffer a nr of chars less or equal that the buffer size and store it in the response content
                httpObject.responseContent.Append(Encoding.ASCII.GetString(httpObject.buffer, 0, byteRead));

                //If the response header was not fully obtained, we go to get the next chunk of data
                if (!HttpUtils.getResponseHeader(httpObject.responseContent.ToString()))
                {
                    clientSocket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, Receiving, httpObject);
                }
                else
                {
                    //Case when the header eas fully obtained - get the body
                    var responseBody = HttpUtils.getResponse(httpObject.responseContent.ToString());

                    //custom header parse used in order to check whether the length of the data received so far has the length specified in the response headers
                    var contentHeaderLength = HttpUtils.getContentLength(httpObject.responseContent.ToString());
                    if(responseBody.Length < contentHeaderLength)
                    {
                        //If it is not, more data is going to be retrieved
                        clientSocket.BeginReceive(httpObject.buffer, 0, HttpObject.BUFFER_SIZE, 0, Receiving, httpObject);
                    }
                    else
                    {
                        //Else, all data was received -> write response details
                        foreach (var i in httpObject.responseContent.ToString().Split('\r', '\n'))
                            Console.WriteLine(i);
                        Console.WriteLine(
                            "{0} --> Response received : expected {1} chars in body, got {2} chars (headers + body)",
                            clientID, contentHeaderLength, httpObject.responseContent.Length);

                        //Shutdown and close the socket
                        clientSocket.Shutdown(SocketShutdown.Both);
                        clientSocket.Close();
                    }
                }
            }
            catch(Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }
    }
}
