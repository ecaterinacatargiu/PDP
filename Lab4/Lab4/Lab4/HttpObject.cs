using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net.Sockets;
using System.Net;
using System.Threading;

namespace Lab4
{
    class HttpObject
    {
        //Socket for client
        public Socket socket = null;

        //The size of the receive buffer + the buffer itself
        public const int BUFFER_SIZE = 512;
        public byte[] buffer = new byte[BUFFER_SIZE];

        //The received data
        public StringBuilder responseContent = new StringBuilder();

        //The unique id of a client
        public int id;

        //The hostname of the server
        public string hostname;

        //Request endpoint
        public string endpoint;

        //IP adrees for server
        public IPEndPoint remoteEndPoint;

        //Mutex for the "Connect" op - initially set to false because it initially has an unsignaled state
        public ManualResetEvent connectDone = new ManualResetEvent(false);

        //Mutex for "Send" op - initially set to false because it initially has an unsignaled state
        public ManualResetEvent sendDone = new ManualResetEvent(false);

        //Mutex for "Receive" op - initially set to false because it initially has an unsignaled state
        public ManualResetEvent receiveDone = new ManualResetEvent(false);
    }
}
