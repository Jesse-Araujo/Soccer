package com.example.soocer.apis

import android.os.Build
import com.example.soocer.auxiliary.dateStringToLocalDateTime
import com.example.soocer.data.EventType
import com.example.soocer.data.Events
import com.example.soocer.data.MarkerLocations
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import java.time.LocalDateTime
import javax.xml.parsers.DocumentBuilderFactory

class FutsalApi {

    companion object {

        fun getFutsalEvents(): List<Events>? {
            val apiKey = "u58gbrmkp3gyf94emqafa3td"
            //val apiUrl = "https://api.sportradar.com/futsal-t1/en/tournaments.xml?api_key=$apiKey" torneios
            //val apiUrl = "https://api.sportradar.com/futsal-t1/en/competitors/sr:competitor:26259/profile.xml?api_key=$apiKey" slb profile


            val apiUrl =
                "https://api.sportradar.com/futsal-t1/en/tournaments/sr:tournament:596/schedule.xml?api_key=$apiKey"


            val client = OkHttpClient()

            val request = Request.Builder()
                .url(apiUrl)
                .build()

            val response = client.newCall(request).execute()
            val events = futsalXmlParser(response.body?.string())
            return events
        }


        fun futsalXmlParser(body: String?) : List<Events>?{
            if (body == null) {
                return null
            }
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(body.byteInputStream())
            document.documentElement.normalize()

            val sportEvents = document.getElementsByTagName("sport_event")
            val events = mutableListOf<Events>()
            for (i in 0 until sportEvents.length) {
                val sportEvent = sportEvents.item(i) as Element

                val leagueName =
                    sportEvent.getElementsByTagName("tournament").item(0)?.attributes?.getNamedItem("name")?.nodeValue ?: ""
                var scheduledDate = sportEvent.getAttribute("scheduled")
                scheduledDate = scheduledDate.split("+")[0]
                scheduledDate = scheduledDate.substring(0,scheduledDate.length-3)

                val homeTeam =
                    sportEvent.getElementsByTagName("competitor").item(0)?.attributes?.getNamedItem(
                        "name"
                    )?.nodeValue ?: ""
                val awayTeam =
                    sportEvent.getElementsByTagName("competitor").item(1)?.attributes?.getNamedItem(
                        "name"
                    )?.nodeValue ?: ""
                var gameId = sportEvent.getAttribute("id")//.toIntOrNull() ?: 0
                gameId = gameId.replace("sr:match:","")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    events.add(
                        Events(
                            gameId.toInt(),
                            EventType.FUTSAL,
                            leagueName,
                            dateStringToLocalDateTime(scheduledDate),
                            "season",
                            "https://static.flashscore.com/res/image/data/MVDIySnd-SSloczGk.png",
                            homeTeam,
                            getFutsalLogos(homeTeam),
                            awayTeam,
                            getFutsalLogos(awayTeam),
                            MarkerLocations.getClubPavilion(homeTeam),
                            Events.isBigGame(homeTeam,awayTeam),
                            0, emptyList<String>().toMutableList()
                        )

                    )
                }
            }
            val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val endDate = currentDate.plusDays(16)

            return events.filter { event ->
                val eventDate = event.date
                eventDate.isEqual(currentDate) || (eventDate.isAfter(currentDate) && eventDate.isBefore(
                    endDate
                ))
            }
        }

        fun getFutsalLogos(club: String): String {
            val c = club.lowercase()
            return when {
                c.contains("benfica") -> "https://www.google.com/url?sa=i&url=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FS.L._Benfica&psig=AOvVaw3KwsWhbTaeWWmEMuqrKqML&ust=1705875064924000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCPjUoPz97IMDFQAAAAAdAAAAABAD"
                c.contains("braga") -> "https://upload.wikimedia.org/wikipedia/pt/e/e4/SCB_AAUM.jpg"
                c.contains("sporting") -> "https://www.google.com/url?sa=i&url=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSporting_CP&psig=AOvVaw3TAOTydY6wKiYl9PjoGKHf&ust=1705875281285000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCJjmuuT-7IMDFQAAAAAdAAAAABAE"
                c.contains("leoes") -> "https://www.google.com/url?sa=i&url=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FLe%25C3%25B5es_de_Porto_Salvo&psig=AOvVaw0VIHuvwMnyQ9kpg0Wa-Ry3&ust=1705875255575000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCJC7j9f-7IMDFQAAAAAdAAAAABAD"
                c.contains("ferreira") -> "https://static.flashscore.com/res/image/data/n1o1ffYg-vipyTxdM.png"
                c.contains("caxinas") -> "https://static.flashscore.com/res/image/data/WSR9UCf5-plmLNqIo.png"
                c.contains("fundao") -> "https://static.flashscore.com/res/image/data/fHN6zqe5-WpHQK9y1.png"
                c.contains("electrico") -> "https://static.flashscore.com/res/image/data/OAMA4Jjl-f9gztEYT.png"
                c.contains("torreense") -> "https://static.flashscore.com/res/image/data/WMb3M5jl-d4t6vb19.png"
                c.contains("quinta") -> "https://static.flashscore.com/res/image/data/AiaSA8Br-C6PPpjnF.png"
                c.contains("belenenses") -> "https://static.flashscore.com/res/image/data/dSqcrLCr-6XfxMu95.png"
                c.contains("candoso") -> "https://static.flashscore.com/res/image/data/pM6CBjDa-WUt12GLI.png"
                else -> ""
            }
        }
    }
}