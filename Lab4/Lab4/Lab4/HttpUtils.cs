using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Lab4
{
    class HttpUtils
    {
        public static readonly int HTTP_PORT = 80;

        public static string getResponse(string responseContent)
        {
            var response = responseContent.Split(new[] { "\r\n\r\n" }, StringSplitOptions.RemoveEmptyEntries);

            return response.Length > 1 ? response[1] : "";
        }

        public static bool getResponseHeader(string responseContent)
        {
            return responseContent.Contains("\r\n\r\n");
        }


        public static int getContentLength(string responseContent)
        {
           var contentLength = 0;
            var responseLines = responseContent.Split('\r', '\n');
            
            foreach(var responseLine in responseLines)
            {
                //Header pattern: < header_name >:< header_value >
                  var headerDetails = responseLine.Split(':');
                if (headerDetails[0].CompareTo("Content-Length") == 0)
                {
                    contentLength = int.Parse(headerDetails[1]);
                }
            }

            return contentLength;

        }

        public static string getRequestString(string hostName, string endPoint)
        {
            return "GET " + endPoint + " HTTP/1.1\r\n" +
                   "Host: " + hostName + "\r\n" +
                   "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36\r\n" +
                   "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,#1#*;q=0.8\r\n" +
                   "Accept-Language: en-US,en;q=0.9,ro;q=0.8\r\n" +
                   //The server will add the content-length header ONLY if the data comes archived (gzip)
                   "Accept-Encoding: gzip, deflate\r\n" +
                   "Connection: keep-alive\r\n" +
                   "Upgrade-Insecure-Requests: 1\r\n" +
                   "Pragma: no-cache\r\n" +
                   "Cache-Control: no-cache\r\n" +
                   "Content-Length: 0\r\n\r\n";

        }

    }
}
