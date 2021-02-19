using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace Lab4
{
    class Program
    {
        private static readonly List<string> HOSTS = new List<string> {
            "www.cs.ubbcluj.ro/~rlupsa/edu/pdp/",
            "www.stackoverflow.com/",
            "www.google.com",
        };
        public static void Main(string[] args)
        {
            DirectCallbacks.run(HOSTS);
            TaskMechanism.run(HOSTS);
            AsyncTaskMechanism.run(HOSTS);
            Console.ReadLine();
        }
    }
}
