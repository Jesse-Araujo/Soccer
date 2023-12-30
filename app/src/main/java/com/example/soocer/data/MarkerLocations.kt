package com.example.soocer.data

import com.example.soocer.events.Events
import com.google.android.gms.maps.model.LatLng
import kotlin.random.Random

enum class Type() {
    STADIUM,
    PAVILION
}

class MarkerLocations(
    val title: String,
    var latLng: LatLng,
    val type: Type,
    val capacity: Int,
    val city: String,
    val expectedCapacity: Int,
    val events : HashSet<Events>
    //val events : MutableList<Events>
) {


    companion object {

        fun getRandomPercentageOfNumber(number: Double): Int {
            val minPercentage = 70.0
            val maxPercentage = 99.0

            // Generate a random percentage within the specified range
            val randomPercentage = Random.nextDouble(minPercentage, maxPercentage)

            // Calculate the result by applying the random percentage to the original number
            val result = number * (randomPercentage / 100.0)

            return result.toInt()
        }

        val markers = mutableListOf<MarkerLocations>(
            /*MarkerLocations("Estádio da Luz", LatLng(38.752663, -9.184720), Type.STADIUM,65000,"Lisboa"),
            MarkerLocations("Pavilhão nº1 da Luz", LatLng(38.75131600625119, -9.183512266644923), Type.PAVILION,"Lisboa"),
            MarkerLocations("Pavilhão nº2 da Luz", LatLng(38.75170132955841, -9.18334996464678), Type.PAVILION,"Lisboa"),
            MarkerLocations("Estádio do Restelo", LatLng(38.702351, -9.207772), Type.STADIUM,"Lisboa"),
            MarkerLocations("Estádio José Alvalade", LatLng(38.761158, -9.160905), Type.STADIUM,"Lisboa"),
            MarkerLocations("Pavilhão João Rocha", LatLng(38.76348, -9.15846), Type.PAVILION,"Lisboa")*/
        )

        fun getClubStadium(club: String): MarkerLocations {
            val c = club.lowercase()
            return when {
                c.contains("benfica") -> MarkerLocations(
                    "Estádio da Luz", LatLng(38.752663, -9.184720), Type.STADIUM, 65000, "Lisboa",
                    getRandomPercentageOfNumber(65000.0), hashSetOf()
                )

                c.contains("braga") -> MarkerLocations(
                    "Estádio Municipal de Braga",
                    LatLng(41.562544, -8.429873),
                    Type.STADIUM,
                    30000,
                    "Braga",
                    getRandomPercentageOfNumber(30000.0), hashSetOf()//mutableListOf()
                )

                c.contains("sporting") -> MarkerLocations(
                    "Estádio José Alvalade",
                    LatLng(38.761158, -9.160905),
                    Type.STADIUM,
                    50000,
                    "Lisboa",
                    getRandomPercentageOfNumber(50000.0), hashSetOf()//mutableListOf()
                )

                c.contains("boavista") -> MarkerLocations(
                    "Estádio do Bessa",
                    LatLng(41.162171338446946, -8.642614747881357),
                    Type.STADIUM,
                    28250,
                    "Porto",
                    getRandomPercentageOfNumber(28250.0), hashSetOf()//mutableListOf()
                )

                c.contains("guima") -> MarkerLocations(
                    "Estádio D. Afonso Henriques",
                    LatLng(41.44584900275168, -8.3009939761841),
                    Type.STADIUM,
                    30000,
                    "Guimarães",
                    getRandomPercentageOfNumber(30000.0), hashSetOf()//mutableListOf()
                )

                c.contains("moreirense") -> MarkerLocations(
                    "Estádio Comendador Joaquim de Almeida Freitas",
                    LatLng(41.378043769166204, -8.354740435702933),
                    Type.STADIUM,
                    6150,
                    "Braga",
                    getRandomPercentageOfNumber(6150.0), hashSetOf()//mutableListOf()
                )

                c.contains("farense") -> MarkerLocations(
                    "Estádio de São Luís",
                    LatLng(37.02289167726662, -7.92852270887837),
                    Type.STADIUM,
                    7000,
                    "Braga",
                    getRandomPercentageOfNumber(7000.0), hashSetOf()//mutableListOf()
                )

                c.contains("famalicao") -> MarkerLocations(
                    "Estádio Municipal 22 de Junho",
                    LatLng(41.40134499011872, -8.52247611851562),
                    Type.STADIUM,
                    5300,
                    "Famalicão",
                    getRandomPercentageOfNumber(5300.0), hashSetOf()//mutableListOf()
                )

                c.contains("estrela") -> MarkerLocations(
                    "Estádio José Gomes",
                    LatLng(38.75195899556411, -9.22790612700795),
                    Type.STADIUM,
                    9300,
                    "Lisboa",
                    getRandomPercentageOfNumber(9300.0), hashSetOf()//mutableListOf()
                )

                c.contains("casa pia") -> MarkerLocations(
                    "Estádio Pina Manique",
                    LatLng(38.73739731148434, -9.203800257623078),
                    Type.STADIUM,
                    2600,
                    "Lisboa",
                    getRandomPercentageOfNumber(2600.0), hashSetOf()//mutableListOf()
                )

                c.contains("portimonense") -> MarkerLocations(
                    "Estádio Municipal de Portimão",
                    LatLng(37.13559878162266, -8.539836303336772),
                    Type.STADIUM,
                    5000,
                    "Portimão",
                    getRandomPercentageOfNumber(5000.0), hashSetOf()//mutableListOf()
                )

                c.contains("estoril") -> MarkerLocations(
                    "Estádio António Coimbra da Mota",
                    LatLng(38.71584483883938, -9.40636906381359),
                    Type.STADIUM,
                    5100,
                    "Amoreira",
                    getRandomPercentageOfNumber(5100.0), hashSetOf()//mutableListOf()
                )

                c.contains("arouca") -> MarkerLocations(
                    "Estádio Municipal de Arouca",
                    LatLng(40.93282852930039, -8.250537293390778),
                    Type.STADIUM,
                    5600,
                    "Amoreira",
                    getRandomPercentageOfNumber(5600.0), hashSetOf()//mutableListOf()
                )

                c.contains("vizela") -> MarkerLocations(
                    "Estádio do FC Vizela",
                    LatLng(41.38843039578033, -8.307188328319052),
                    Type.STADIUM,
                    6000,
                    "Vizela",
                    getRandomPercentageOfNumber(6000.0), hashSetOf()//mutableListOf()
                )

                c.contains("rio ave") -> MarkerLocations(
                    "Estádio do Rio Ave Futebol Clube",
                    LatLng(41.36264054692577, -8.740318408714279),
                    Type.STADIUM,
                    5250,
                    "Vila do Conde",
                    getRandomPercentageOfNumber(5250.0), hashSetOf()//mutableListOf()
                )

                c.contains("gil vicente") -> MarkerLocations(
                    "Estádio Cidade de Barcelos",
                    LatLng(41.55108909526628, -8.622998210929838),
                    Type.STADIUM,
                    12500,
                    "Barcelos",
                    getRandomPercentageOfNumber(12500.0), hashSetOf()//mutableListOf()
                )

                c.contains("chaves") -> MarkerLocations(
                    "Estádio Municipal Engenheiro Manuel Branco Teixeira",
                    LatLng(41.75054615664043, -7.465004201315336),
                    Type.STADIUM,
                    8400,
                    "Chaves",
                    getRandomPercentageOfNumber(8400.0), hashSetOf()//mutableListOf()
                )

                else -> MarkerLocations(
                    "Estádio do Dragão",
                    LatLng(41.161745739580674, -8.583816308722257),
                    Type.STADIUM,
                    50000,
                    "Porto",
                    getRandomPercentageOfNumber(50000.0), hashSetOf()//mutableListOf()
                )
            }
        }

        fun getClubPavilion(club: String): MarkerLocations {
            val c = club.lowercase()
            when {
                c.contains("benfica") -> return MarkerLocations(
                    "Pavilhão nº2 da Luz",
                    LatLng(38.75170132955841, -9.18334996464678),
                    Type.PAVILION,
                    1800,
                    "Lisboa",
                    getRandomPercentageOfNumber(1800.0), hashSetOf()//mutableListOf()
                )
                c.contains("sporting") -> return MarkerLocations(
                    "Pavilhão João Rocha",
                    LatLng(38.76348, -9.15846),
                    Type.PAVILION,
                    3000,
                    "Lisboa",
                    getRandomPercentageOfNumber(3000.0), hashSetOf()//mutableListOf()
                )
                c.contains("porto") -> return MarkerLocations(
                    "Dragão Arena",
                    LatLng(41.16262562453169, -8.581726406850882),
                    Type.PAVILION,
                    2200,
                    "Porto",
                    getRandomPercentageOfNumber(2200.0), hashSetOf()//mutableListOf()
                )
                c.contains("abc") -> return MarkerLocations(
                    "Pavilhão Flávio Sá Leite",
                    LatLng(41.53991280563391, -8.419730719663232),
                    Type.PAVILION,
                    5000,
                    "Braga",
                    getRandomPercentageOfNumber(5000.0), hashSetOf()//mutableListOf()
                )
                c.contains("aguas santas") -> return MarkerLocations(
                    "Pavilhão do Águas Santas",
                    LatLng(41.20599393333701, -8.565669191726352),
                    Type.PAVILION,
                    1000,
                    "Porto",
                    getRandomPercentageOfNumber(1000.0), hashSetOf()//mutableListOf()
                )
                c.contains("madeira") -> return MarkerLocations(
                    "Pavilhão do CS Marítimo",
                    LatLng(32.671795014929955, -16.935586840635324),
                    Type.PAVILION,
                    1000,
                    "Funchal",
                    getRandomPercentageOfNumber(1000.0), hashSetOf()//mutableListOf()
                )
                c == "vitoria" -> return MarkerLocations(
                    "Pavilhão Antoine Velge",
                    LatLng(38.53144722350263, -8.889980869201684),
                    Type.PAVILION,
                    1200,
                    "Setúbal",
                    getRandomPercentageOfNumber(1200.0), hashSetOf()//mutableListOf()
                )
                c == "vitoria sc" -> return MarkerLocations(
                    "Pavilhão Vitória SC",
                    LatLng(41.44809066106037, -8.280449839504874),
                    Type.PAVILION,
                    2500,
                    "Guimarães",
                    getRandomPercentageOfNumber(2500.0), hashSetOf()//mutableListOf()
                )
                c.contains("belenenses") -> return MarkerLocations(
                    "Pavilhão Acácio Rosa",
                    LatLng(38.70150037024289, -9.210051323381323),
                    Type.PAVILION,
                    1700,
                    "Lisboa",
                    getRandomPercentageOfNumber(1700.0), hashSetOf()//mutableListOf()
                )
                c.contains("povoa") -> return MarkerLocations(
                    "Pavilhão Desportivo Municipal da Póvoa de Varzim",
                    LatLng(41.389852590723194, -8.761240394769919),
                    Type.PAVILION,
                    2500,
                    "Póvoa de Varzim",
                    getRandomPercentageOfNumber(2500.0), hashSetOf()//mutableListOf()
                )
                c.contains("avanca") -> return MarkerLocations(
                    "Pavilhão Municipal Comendador Adelino Dias Costa",
                    LatLng(40.80961425158267, -8.576722114560576),
                    Type.PAVILION,
                    5000,
                    "Avanca",
                    getRandomPercentageOfNumber(5000.0), hashSetOf()//mutableListOf()
                )
                else -> return MarkerLocations(
                    "Pavilhão Desportivo Municipal de Vila Nova de Gaia",
                    LatLng(41.11807460945089, -8.59316885960061),
                    Type.PAVILION,
                    2000,
                    "Vila Nova de Gaia",
                    getRandomPercentageOfNumber(2000.0), hashSetOf()//mutableListOf()
                )
            }
        }
    }
}
