using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Net.Sockets;
using System.IO;
using System.Diagnostics;


string dbg = "";
TcpClient tcpclnt = null;

while (true)
{
    try
    {
        Console.WriteLine("Connecting.....");

        tcpclnt = new TcpClient();
        tcpclnt.Connect("202.129.173.59", 9661);
        // use the ipaddress as in the server program

        Console.WriteLine("Connected");
        Stream stm = tcpclnt.GetStream();

        //ASCIIEncoding asen = new ASCIIEncoding();
        byte[] server_key = { 0x01, 0x43, 0x47, 0x56, 0x54, 0x4D, 0x53, 0x00, 0x43, 0x47, 0x56, 0x54, 0x4D, 0x53, 0x00 };
        Console.WriteLine("Transmitting.....");

        stm.Write(server_key, 0, server_key.Length);


        while (true)
        {
            byte[] resp = new byte[1024];
            int k = stm.Read(resp, 0, 1024);
            string nmea = Encoding.UTF8.GetString(resp);

            string[] nmeas = nmea.Split("\n");
            string msgType5 = "";

            foreach (var item in nmeas)
            {
                Console.WriteLine(item.Trim());

                if (item.EndsWith("\r"))
                {
                    dbg = item;
                    string[] data = item.Split(",");

                    if (Int32.Parse(data[1]) == 1)
                    {
                        Process process = new Process();
                        // Configure the process using the StartInfo properties.
                        process.StartInfo.FileName = "/opt/homebrew/bin/python3";
                        process.StartInfo.Arguments = "/Users/zultan/sources/python/AIS_decoder.py " + item.Trim();
                        process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                        process.Start();
                        process.WaitForExit(); // Waits here for the process to exit.
                    }
                    else
                    {
                        if (Int32.Parse(data[2]) == 1)
                        {
                            msgType5 = item.Trim();
                        }
                        else
                        {
                            if (!String.IsNullOrEmpty(msgType5))
                            {
                                Process process = new Process();
                                // Configure the process using the StartInfo properties.
                                process.StartInfo.FileName = "/opt/homebrew/bin/python3";
                                process.StartInfo.Arguments = "/Users/zultan/sources/python/AIS_decoder.py " + msgType5 + " " + item.Trim();
                                process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                                process.Start();
                                process.WaitForExit(); // Waits here for the process to exit.

                                msgType5 = "";
                            }
                        }
                    }
                }
            }

            Thread.Sleep(0);
        }

        tcpclnt.Close();
    }

    catch (Exception e)
    {
        Console.WriteLine("Error..... :: " + dbg + " ---> " + e.StackTrace);
        tcpclnt.Close();
    }
}


