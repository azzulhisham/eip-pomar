using System;
using System.Net.Http;
using System.Net.Http.Json;
using System.Reflection.Metadata;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace ConsoleApp
{
    public static class Program
    {
        public static async Task Main(string[] args)
        {
            //Console.WriteLine(args[1]);

            if(args.Length > 1)
            {
                var client = new HttpClient();
                string requestUrl = "https://mmdis.marine.gov.my/MMDIS_DIS_STG/spk/getVessel";

                var request = new HttpRequestMessage()
                {
                    RequestUri = new Uri(requestUrl),
                    Method = HttpMethod.Get,
                };

                var mmdisVesselsDet = new
                {
                    imoNumber = args[1],
                    officialNumber = "",
                    vesselId = "",
                    vesselName = ""
                };

                request.Headers.Add("Accept", "application/json");
                request.Content = new StringContent(JsonSerializer.Serialize(mmdisVesselsDet), Encoding.UTF8, "application/json");

                var response = await client.SendAsync(request);

                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    var content = await response.Content.ReadAsStringAsync();
                    //Vtmis.WebAdmin.MMDIS.Targets.Dto.MmdisApiGetVesselDto mmdisApiGetVesselDto = JsonSerializer.Deserialize<Vtmis.WebAdmin.MMDIS.Targets.Dto.MmdisApiGetVesselDto>(content);

                    if (content != null && !string.IsNullOrEmpty(content))
                    {
                        Console.WriteLine(content.Replace("\"code\":\"00\",\"content\":[", "\"result\":").Replace("]",""));
                    }
                    else
                    {
                        //return BadRequest();
                    }
                }
                else
                {
                    //return BadRequest();
                }
            }
            else
            {
                var badRequest = new
                {
                    code = "400",
                    content = "Bad Request"
                };

                Console.WriteLine(JsonSerializer.Serialize(badRequest));

            }

        }
    }
}
