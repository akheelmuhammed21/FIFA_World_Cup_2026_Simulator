import java.util.*;

public class SquadData {
    private static final Player.Position GK  = Player.Position.GK;
    private static final Player.Position DEF = Player.Position.DEF;
    private static final Player.Position MID = Player.Position.MID;
    private static final Player.Position FWD = Player.Position.FWD;
    private static final Player.Fitness  FIT   = Player.Fitness.FIT;
    private static final Player.Fitness  DOUBT = Player.Fitness.DOUBT;
    private static final Player.Fitness  OUT   = Player.Fitness.OUT;

    /**
     * Player data sourced from Transfermarkt (values, fitness) and FotMob (form).
     * Form scale: -1.0 (very poor) to +1.0 (exceptional). Data as of May 2026.
     * isStar = marquee players whose presence/absence materially affects match outcomes.
     */
    public static void attachAll(List<Team> teams) {
        Map<String, Squad> map = buildAll();
        for (Team t : teams)
            if (map.containsKey(t.name))
                t.squad = map.get(t.name);
    }

    private static Map<String, Squad> buildAll() {
        Map<String, Squad> m = new HashMap<>();
        // UEFA
        m.put("France",            france());
        m.put("Spain",             spain());
        m.put("England",           england());
        m.put("Portugal",          portugal());
        m.put("Germany",           germany());
        m.put("Netherlands",       netherlands());
        m.put("Belgium",           belgium());
        m.put("Norway",            norway());
        m.put("Croatia",           croatia());
        m.put("Switzerland",       switzerland());
        m.put("Scotland",          scotland());
        m.put("Sweden",            sweden());
        m.put("Austria",           austria());
        m.put("Turkey",            turkey());
        m.put("Czechia",           czechia());
        m.put("Bosnia Herzegovina",bosniaHerzegovina());
        // CONMEBOL
        m.put("Argentina",  argentina());
        m.put("Brazil",     brazil());
        m.put("Colombia",   colombia());
        m.put("Uruguay",    uruguay());
        m.put("Ecuador",    ecuador());
        m.put("Paraguay",   paraguay());
        // CONCACAF
        m.put("USA",     usa());
        m.put("Mexico",  mexico());
        m.put("Canada",  canada());
        m.put("Panama",  panama());
        m.put("Haiti",   haiti());
        m.put("Curacao", curacao());
        // CAF
        m.put("Morocco",      morocco());
        m.put("Senegal",      senegal());
        m.put("Algeria",      algeria());
        m.put("Ivory Coast",  ivoryCoast());
        m.put("Egypt",        egypt());
        m.put("DR Congo",     drCongo());
        m.put("Ghana",        ghana());
        m.put("Tunisia",      tunisia());
        m.put("Cape Verde",   capeVerde());
        m.put("South Africa", southAfrica());
        // AFC
        m.put("Japan",        japan());
        m.put("South Korea",  southKorea());
        m.put("Australia",    australia());
        m.put("Saudi Arabia", saudiArabia());
        m.put("Iran",         iran());
        m.put("Uzbekistan",   uzbekistan());
        m.put("Iraq",         iraq());
        m.put("Qatar",        qatar());
        m.put("Jordan",       jordan());
        // OFC
        m.put("New Zealand",  newZealand());
        return m;
    }

    // ── UEFA ──────────────────────────────────────────────────────────────────────

    private static Squad france() {
        return squad("France",
            p("Mike Maignan",         GK,  "AC Milan",           29,  0.50, FIT,   40, true),
            p("William Saliba",       DEF, "Arsenal",            24,  0.60, FIT,   80, true),
            p("Dayot Upamecano",      DEF, "Bayern Munich",      26,  0.30, FIT,   55, false),
            p("Jules Kounde",         DEF, "Barcelona",          26,  0.40, FIT,   70, true),
            p("Theo Hernandez",       DEF, "AC Milan",           27,  0.40, FIT,   65, false),
            p("Aurelien Tchouameni",  MID, "Real Madrid",        25,  0.40, FIT,   80, true),
            p("Eduardo Camavinga",    MID, "Real Madrid",        22,  0.50, FIT,   80, true),
            p("Warren Zaire-Emery",   MID, "PSG",                19,  0.50, FIT,   60, false),
            p("Antoine Griezmann",    MID, "Atletico Madrid",    34,  0.30, FIT,   20, true),
            p("Kylian Mbappe",        FWD, "Real Madrid",        26,  0.70, FIT,  180, true),
            p("Ousmane Dembele",      FWD, "PSG",                28,  0.50, FIT,   60, true),
            p("Marcus Thuram",        FWD, "Inter Milan",        27,  0.60, FIT,   70, true)
        );
    }

    private static Squad spain() {
        return squad("Spain",
            p("Unai Simon",           GK,  "Athletic Bilbao",    27,  0.30, FIT,   30, true),
            p("Robin Le Normand",     DEF, "Atletico Madrid",    28,  0.30, FIT,   35, false),
            p("Alejandro Grimaldo",   DEF, "Bayer Leverkusen",   29,  0.50, FIT,   40, false),
            p("Marc Cucurella",       DEF, "Chelsea",            26,  0.30, FIT,   35, false),
            p("Dani Carvajal",        DEF, "Real Madrid",        33, -0.50, OUT,   20, true),
            p("Dani Olmo",            MID, "Barcelona",          27,  0.60, FIT,   80, true),
            p("Pedri",                MID, "Barcelona",          22,  0.40, DOUBT,100, true),
            p("Rodri",                MID, "Man City",           29, -0.50, OUT,  120, true),
            p("Martin Zubimendi",     MID, "Liverpool",          26,  0.50, FIT,   70, true),
            p("Lamine Yamal",         FWD, "Barcelona",          17,  0.90, FIT,  180, true),
            p("Nico Williams",        FWD, "Athletic Bilbao",    22,  0.80, FIT,  100, true),
            p("Mikel Oyarzabal",      FWD, "Real Sociedad",      28,  0.30, FIT,   45, false)
        );
    }

    private static Squad england() {
        return squad("England",
            p("Jordan Pickford",      GK,  "Everton",            31,  0.20, FIT,   20, true),
            p("Marc Guehi",           DEF, "Crystal Palace",     24,  0.30, FIT,   55, false),
            p("John Stones",          DEF, "Man City",           31,  0.00, DOUBT, 35, false),
            p("Trent Alexander-Arnold",MID,"Real Madrid",        26,  0.50, FIT,   80, true),
            p("Declan Rice",          MID, "Arsenal",            26,  0.70, FIT,  100, true),
            p("Jude Bellingham",      MID, "Real Madrid",        21,  0.80, FIT,  180, true),
            p("Kobbie Mainoo",        MID, "Man United",         20,  0.40, FIT,   55, false),
            p("Phil Foden",           FWD, "Man City",           25,  0.60, FIT,  130, true),
            p("Bukayo Saka",          FWD, "Arsenal",            23,  0.70, FIT,  150, true),
            p("Harry Kane",           FWD, "Bayern Munich",      31,  0.70, FIT,   80, true),
            p("Cole Palmer",          FWD, "Chelsea",            23,  0.80, FIT,  120, true),
            p("Ollie Watkins",        FWD, "Aston Villa",        29,  0.50, FIT,   60, false)
        );
    }

    private static Squad portugal() {
        return squad("Portugal",
            p("Diogo Costa",          GK,  "Porto",              25,  0.40, FIT,   40, true),
            p("Ruben Dias",           DEF, "Man City",           28,  0.50, FIT,   70, true),
            p("Joao Cancelo",         DEF, "Barcelona",          31,  0.30, FIT,   25, false),
            p("Nuno Mendes",          DEF, "PSG",                22,  0.20, DOUBT, 60, false),
            p("Diogo Dalot",          DEF, "Man United",         26,  0.30, FIT,   40, false),
            p("Bruno Fernandes",      MID, "Man United",         30,  0.40, FIT,   55, true),
            p("Bernardo Silva",       MID, "Man City",           30,  0.60, FIT,   80, true),
            p("Joao Neves",           MID, "PSG",                20,  0.70, FIT,   80, true),
            p("Vitinha",              MID, "PSG",                25,  0.50, FIT,   65, false),
            p("Rafael Leao",          FWD, "AC Milan",           25,  0.50, FIT,   80, true),
            p("Goncalo Ramos",        FWD, "PSG",                23,  0.40, FIT,   60, false),
            p("Francisco Conceicao",  FWD, "Juventus",           22,  0.50, FIT,   45, false)
        );
    }

    private static Squad germany() {
        return squad("Germany",
            p("Manuel Neuer",         GK,  "Bayern Munich",      39,  0.00, FIT,    5, true),
            p("Antonio Rudiger",      DEF, "Real Madrid",        32,  0.20, FIT,   20, true),
            p("Jonathan Tah",         DEF, "Bayer Leverkusen",   29,  0.40, FIT,   40, false),
            p("Joshua Kimmich",       DEF, "Bayern Munich",      30,  0.50, FIT,   65, true),
            p("Maximilian Mittelstadt",DEF,"Stuttgart",          27,  0.30, FIT,   28, false),
            p("Florian Wirtz",        MID, "Bayer Leverkusen",   22,  0.90, FIT,  180, true),
            p("Jamal Musiala",        MID, "Bayern Munich",      22,  0.80, FIT,  150, true),
            p("Robert Andrich",       MID, "Bayer Leverkusen",   30,  0.30, FIT,   35, false),
            p("Kai Havertz",          FWD, "Arsenal",            26,  0.50, FIT,   65, true),
            p("Leroy Sane",           FWD, "Bayern Munich",      29,  0.20, FIT,   40, false),
            p("Niclas Fullkrug",      FWD, "West Ham",           32,  0.00, DOUBT, 25, false)
        );
    }

    private static Squad netherlands() {
        return squad("Netherlands",
            p("Bart Verbruggen",      GK,  "Brighton",           22,  0.30, FIT,   25, false),
            p("Virgil van Dijk",      DEF, "Liverpool",          33,  0.40, FIT,   25, true),
            p("Jurrien Timber",       DEF, "Arsenal",            23,  0.50, FIT,   50, true),
            p("Denzel Dumfries",      DEF, "Inter Milan",        28,  0.30, FIT,   35, false),
            p("Nathan Ake",           DEF, "Man City",           30,  0.20, FIT,   30, false),
            p("Ryan Gravenberch",     MID, "Liverpool",          22,  0.60, FIT,   65, true),
            p("Tijjani Reijnders",    MID, "AC Milan",           26,  0.50, FIT,   55, true),
            p("Xavi Simons",          MID, "RB Leipzig",         22,  0.60, FIT,   80, true),
            p("Frenkie de Jong",      MID, "Barcelona",          27,  0.10, DOUBT, 60, true),
            p("Cody Gakpo",           FWD, "Liverpool",          25,  0.50, FIT,   65, true),
            p("Brian Brobbey",        FWD, "Ajax",               23,  0.30, FIT,   35, false)
        );
    }

    private static Squad belgium() {
        return squad("Belgium",
            p("Matz Sels",            GK,  "Nottm Forest",       32,  0.30, FIT,   10, false),
            p("Wout Faes",            DEF, "Leicester",          26,  0.20, FIT,   18, false),
            p("Arthur Theate",        DEF, "Rennes",             24,  0.30, FIT,   20, false),
            p("Amadou Onana",         MID, "Aston Villa",        23,  0.50, FIT,   55, true),
            p("Youri Tielemans",      MID, "Aston Villa",        27,  0.40, FIT,   35, true),
            p("Kevin De Bruyne",      MID, "Man City",           34,  0.30, DOUBT, 30, true),
            p("Johan Bakayoko",       FWD, "PSV Eindhoven",      22,  0.50, FIT,   35, false),
            p("Romelu Lukaku",        FWD, "Napoli",             31,  0.30, FIT,   22, true),
            p("Lois Openda",          FWD, "RB Leipzig",         24,  0.60, FIT,   60, true),
            p("Leandro Trossard",     FWD, "Arsenal",            30,  0.40, FIT,   30, false)
        );
    }

    private static Squad norway() {
        return squad("Norway",
            p("Orjan Nyland",         GK,  "Inter Milan",        36,  0.20, FIT,    5, false),
            p("Leo Ostigard",         DEF, "Napoli",             25,  0.30, FIT,   25, false),
            p("Kristoffer Ajer",      DEF, "Brentford",          26,  0.30, FIT,   20, true),
            p("Sander Berge",         MID, "Fulham",             27,  0.30, FIT,   25, true),
            p("Martin Odegaard",      MID, "Arsenal",            27,  0.60, DOUBT,100, true),
            p("Antonio Nusa",         FWD, "RB Leipzig",         20,  0.50, FIT,   40, true),
            p("Alexander Sorloth",    FWD, "Atletico Madrid",    28,  0.40, FIT,   35, true),
            p("Erling Haaland",       FWD, "Man City",           25,  0.90, FIT,  200, true)
        );
    }

    private static Squad croatia() {
        return squad("Croatia",
            p("Dominik Livakovic",    GK,  "Fenerbahce",         29,  0.20, FIT,   18, true),
            p("Josko Gvardiol",       DEF, "Man City",           22,  0.50, FIT,   80, true),
            p("Borna Sosa",           DEF, "Stuttgart",          26,  0.20, FIT,   20, false),
            p("Mateo Kovacic",        MID, "Man City",           30,  0.30, FIT,   45, true),
            p("Luka Modric",          MID, "Real Madrid",        40, -0.10, FIT,    5, true),
            p("Marcelo Brozovic",     MID, "Al-Nassr",           32,  0.10, FIT,   15, false),
            p("Andrej Kramaric",      FWD, "Hoffenheim",         33,  0.20, FIT,   12, false)
        );
    }

    private static Squad switzerland() {
        return squad("Switzerland",
            p("Yann Sommer",          GK,  "Inter Milan",        36,  0.30, FIT,    8, true),
            p("Manuel Akanji",        DEF, "Man City",           29,  0.40, FIT,   45, true),
            p("Remo Freuler",         MID, "Nottm Forest",       32,  0.30, FIT,   15, false),
            p("Granit Xhaka",         MID, "Bayer Leverkusen",   32,  0.40, FIT,   20, true),
            p("Xherdan Shaqiri",      FWD, "Basel",              33, -0.10, FIT,    4, false),
            p("Breel Embolo",         FWD, "Monaco",             27,  0.20, FIT,   25, false),
            p("Zeki Amdouni",         FWD, "Burnley",            23,  0.30, FIT,   20, false)
        );
    }

    private static Squad scotland() {
        return squad("Scotland",
            p("Craig Gordon",         GK,  "Hearts",             41,  0.00, FIT,    2, false),
            p("Andrew Robertson",     DEF, "Liverpool",          31,  0.40, FIT,   35, true),
            p("Kieran Tierney",       DEF, "Real Sociedad",      27,  0.20, DOUBT, 25, false),
            p("Scott McTominay",      MID, "Napoli",             27,  0.50, FIT,   50, true),
            p("John McGinn",          MID, "Aston Villa",        30,  0.30, FIT,   30, true),
            p("Ryan Christie",        MID, "Bournemouth",        29,  0.20, FIT,   15, false),
            p("Lyndon Dykes",         FWD, "QPR",                29,  0.10, FIT,    8, false)
        );
    }

    private static Squad sweden() {
        return squad("Sweden",
            p("Robin Olsen",          GK,  "Aston Villa",        35,  0.10, FIT,    5, false),
            p("Isak Hien",            DEF, "Atalanta",           25,  0.40, FIT,   30, true),
            p("Emil Krafth",          DEF, "Newcastle",          30,  0.20, FIT,   10, false),
            p("Dejan Kulusevski",     MID, "Tottenham",          25,  0.50, FIT,   70, true),
            p("Emil Forsberg",        MID, "RB Leipzig",         29,  0.20, FIT,   15, false),
            p("Viktor Gyokeres",      FWD, "Arsenal",            27,  0.90, FIT,  150, true),
            p("Alexander Isak",       FWD, "Newcastle",          26,  0.80, FIT,  130, true)
        );
    }

    private static Squad austria() {
        return squad("Austria",
            p("Patrick Pentz",        GK,  "Real Betis",         27,  0.20, FIT,   10, false),
            p("Stefan Posch",         DEF, "Bologna",            27,  0.20, FIT,   20, false),
            p("Marcel Sabitzer",      MID, "Dortmund",           30,  0.40, FIT,   30, true),
            p("Christoph Baumgartner",MID, "RB Leipzig",         25,  0.40, FIT,   35, true),
            p("Michael Gregoritsch",  FWD, "Freiburg",           30,  0.20, FIT,   20, false),
            p("Marko Arnautovic",     FWD, "Inter Milan",        37,  0.10, FIT,    5, false)
        );
    }

    private static Squad turkey() {
        return squad("Turkey",
            p("Mert Gunok",           GK,  "Besiktas",           35,  0.10, FIT,    4, false),
            p("Samet Akaydin",        DEF, "Real Betis",         29,  0.20, FIT,   15, false),
            p("Hakan Calhanoglu",     MID, "Inter Milan",        30,  0.50, FIT,   45, true),
            p("Arda Guler",           MID, "Real Madrid",        19,  0.60, FIT,   80, true),
            p("Kerem Akturkoglu",     FWD, "Galatasaray",        25,  0.30, FIT,   30, true),
            p("Baris Alper Yilmaz",   FWD, "Galatasaray",        24,  0.30, FIT,   22, false)
        );
    }

    private static Squad czechia() {
        return squad("Czechia",
            p("Jindrich Stanek",      GK,  "Sevilla",            26,  0.20, FIT,   10, false),
            p("Vladimir Coufal",      DEF, "West Ham",           32,  0.20, FIT,   12, false),
            p("Ladislav Krejci",      DEF, "Bologna",            24,  0.30, FIT,   15, true),
            p("Tomas Soucek",         MID, "West Ham",           31,  0.40, FIT,   30, true),
            p("Antonin Barak",        MID, "Fiorentina",         30,  0.30, FIT,   20, false),
            p("Adam Hlozek",          FWD, "Bayer Leverkusen",   23,  0.40, FIT,   35, true),
            p("Patrik Schick",        FWD, "Bayer Leverkusen",   30,  0.30, DOUBT, 40, true)
        );
    }

    private static Squad bosniaHerzegovina() {
        return squad("Bosnia Herzegovina",
            p("Nikola Vasilj",        GK,  "Sporting CP",        28,  0.30, FIT,    8, false),
            p("Anel Ahmedhodzic",     DEF, "Rangers",            24,  0.40, FIT,   20, true),
            p("Strahinja Tanovic",    DEF, "Red Star Belgrade",  22,  0.20, FIT,    5, false),
            p("Rade Krunic",          MID, "Fenerbahce",         31,  0.30, FIT,   15, false),
            p("Benjamin Tahirovic",   MID, "Roma",               21,  0.40, FIT,   18, true),
            p("Ermedin Demirovic",    FWD, "Stuttgart",          27,  0.50, FIT,   35, true)
        );
    }

    // ── CONMEBOL ──────────────────────────────────────────────────────────────────

    private static Squad argentina() {
        return squad("Argentina",
            p("Emiliano Martinez",    GK,  "Aston Villa",        32,  0.30, FIT,   25, true),
            p("Lisandro Martinez",    DEF, "Man United",         27,  0.40, FIT,   60, true),
            p("Cristian Romero",      DEF, "Tottenham",          26,  0.30, DOUBT, 65, true),
            p("Enzo Fernandez",       MID, "Chelsea",            24,  0.40, FIT,   80, true),
            p("Alexis Mac Allister",  MID, "Liverpool",          26,  0.50, FIT,   75, true),
            p("Julian Alvarez",       FWD, "Atletico Madrid",    25,  0.70, FIT,  100, true),
            p("Lautaro Martinez",     FWD, "Inter Milan",        27,  0.60, FIT,  100, true),
            p("Lionel Messi",         FWD, "Inter Miami",        38,  0.50, DOUBT, 30, true),
            p("Alejandro Garnacho",   FWD, "Man United",         20,  0.60, FIT,   70, true),
            p("Nahuel Molina",        DEF, "Atletico Madrid",    26,  0.20, FIT,   30, false)
        );
    }

    private static Squad brazil() {
        return squad("Brazil",
            p("Alisson",              GK,  "Liverpool",          32,  0.40, FIT,   35, true),
            p("Gabriel Magalhaes",    DEF, "Arsenal",            27,  0.50, FIT,   65, true),
            p("Marquinhos",           DEF, "PSG",                31,  0.20, FIT,   35, false),
            p("Bruno Guimaraes",      MID, "Newcastle",          27,  0.60, FIT,   80, true),
            p("Lucas Paqueta",        MID, "West Ham",           27,  0.30, FIT,   65, true),
            p("Rodrygo",              FWD, "Real Madrid",        24,  0.50, FIT,  100, true),
            p("Vinicius Jr",          FWD, "Real Madrid",        24,  0.80, FIT,  180, true),
            p("Raphinha",             FWD, "Barcelona",          28,  0.70, FIT,   80, true),
            p("Endrick",              FWD, "Real Madrid",        18,  0.50, FIT,   60, false),
            p("Savinho",              FWD, "Man City",           21,  0.50, FIT,   55, false)
        );
    }

    private static Squad colombia() {
        return squad("Colombia",
            p("Camilo Vargas",        GK,  "Atlas",              32,  0.00, FIT,    2, false),
            p("Davinson Sanchez",     DEF, "Galatasaray",        28,  0.20, FIT,   20, false),
            p("Daniel Munoz",         DEF, "Crystal Palace",     28,  0.30, FIT,   20, true),
            p("Richard Rios",         MID, "Palmeiras",          25,  0.40, FIT,   22, true),
            p("James Rodriguez",      MID, "Rayo Vallecano",     33,  0.20, FIT,    8, true),
            p("Luis Diaz",            FWD, "Liverpool",          28,  0.70, FIT,   80, true),
            p("Cucho Hernandez",      FWD, "Columbus Crew",      26,  0.30, FIT,   15, false)
        );
    }

    private static Squad uruguay() {
        return squad("Uruguay",
            p("Sergio Rochet",        GK,  "Nacional",           31,  0.10, FIT,    4, false),
            p("Jose Maria Gimenez",   DEF, "Atletico Madrid",    30,  0.30, FIT,   30, true),
            p("Ronald Araujo",        DEF, "Barcelona",          25,  0.10, DOUBT, 60, true),
            p("Federico Valverde",    MID, "Real Madrid",        26,  0.80, FIT,  120, true),
            p("Manuel Ugarte",        MID, "Man United",         23,  0.40, FIT,   50, true),
            p("Darwin Nunez",         FWD, "Liverpool",          25,  0.40, FIT,   75, true),
            p("Luis Suarez",          FWD, "River Plate",        39,  0.00, FIT,    2, false)
        );
    }

    private static Squad ecuador() {
        return squad("Ecuador",
            p("Hernan Galindez",      GK,  "Aucas",              37,  0.00, FIT,    1, false),
            p("Piero Hincapie",       DEF, "Bayer Leverkusen",   22,  0.30, FIT,   40, true),
            p("Moises Caicedo",       MID, "Chelsea",            22,  0.60, FIT,   90, true),
            p("Kendry Paez",          MID, "Chelsea",            17,  0.40, FIT,   35, true),
            p("Gonzalo Plata",        FWD, "Real Valladolid",    23,  0.20, FIT,   12, false),
            p("Enner Valencia",       FWD, "Internacional",      35,  0.10, FIT,    4, false)
        );
    }

    private static Squad paraguay() {
        return squad("Paraguay",
            p("Antony Silva",         GK,  "Cerro Porteno",      38,  0.00, FIT,    1, false),
            p("Junior Alonso",        DEF, "Atletico Mineiro",   29,  0.10, FIT,    8, false),
            p("Miguel Almiron",       MID, "Newcastle",          30,  0.20, FIT,   20, true),
            p("Mathias Villasanti",   MID, "Gremio",             26,  0.20, FIT,   12, false),
            p("Antonio Sanabria",     FWD, "Torino",             28,  0.10, FIT,   15, false)
        );
    }

    // ── CONCACAF ──────────────────────────────────────────────────────────────────

    private static Squad usa() {
        return squad("USA",
            p("Matt Turner",          GK,  "Crystal Palace",     30,  0.10, FIT,   10, false),
            p("Antonee Robinson",     DEF, "Fulham",             27,  0.40, FIT,   25, true),
            p("Joe Scally",           DEF, "Borussia MG",        22,  0.20, FIT,   12, false),
            p("Tyler Adams",          MID, "Bournemouth",        26,  0.10, DOUBT, 30, true),
            p("Yunus Musah",          MID, "AC Milan",           22,  0.30, FIT,   35, true),
            p("Christian Pulisic",    FWD, "AC Milan",           26,  0.50, FIT,   45, true),
            p("Ricardo Pepi",         FWD, "PSV Eindhoven",      22,  0.30, FIT,   20, false),
            p("Folarin Balogun",      FWD, "Monaco",             23,  0.30, FIT,   25, false)
        );
    }

    private static Squad canada() {
        return squad("Canada",
            p("Maxime Crepeau",       GK,  "LA Galaxy",          30,  0.10, FIT,    4, false),
            p("Alistair Johnston",    DEF, "Celtic",             26,  0.30, FIT,   12, false),
            p("Kamal Miller",         DEF, "CF Montreal",        27,  0.10, FIT,    5, false),
            p("Stephen Eustaquio",    MID, "Porto",              27,  0.40, FIT,   18, true),
            p("Alphonso Davies",      FWD, "Bayern Munich",      24,  0.70, FIT,   80, true),
            p("Jonathan David",       FWD, "Lille",              25,  0.80, FIT,   65, true),
            p("Cyle Larin",           FWD, "Real Valladolid",    29,  0.10, FIT,    8, false)
        );
    }

    private static Squad mexico() {
        return squad("Mexico",
            p("Luis Malagon",         GK,  "Club America",       25,  0.20, FIT,    5, false),
            p("Edson Alvarez",        MID, "West Ham",           27,  0.30, FIT,   30, true),
            p("Jorge Sanchez",        DEF, "Ajax",               26,  0.10, FIT,    8, false),
            p("Cesar Montes",         DEF, "Espanyol",           27,  0.10, FIT,    6, false),
            p("Hirving Lozano",       FWD, "PSV Eindhoven",      29,  0.20, FIT,   20, true),
            p("Santiago Gimenez",     FWD, "Feyenoord",          23,  0.60, FIT,   40, true),
            p("Raul Jimenez",         FWD, "Fulham",             33,  0.10, FIT,   10, false)
        );
    }

    private static Squad panama() {
        return squad("Panama",
            p("Luis Mejia",           GK,  "Modena",             29,  0.10, FIT,    2, false),
            p("Harold Cummings",      DEF, "Santos Laguna",      26,  0.10, FIT,    5, false),
            p("Adalberto Carrasquilla",MID,"Watford",            25,  0.20, FIT,    8, true),
            p("Cecilio Waterman",     FWD, "Penarol",            29,  0.10, FIT,    5, false)
        );
    }

    private static Squad haiti() {
        return squad("Haiti",
            p("Carlens Arcus",        GK,  "AS Valcourt",        30,  0.00, FIT,    1, false),
            p("Mechack Jerome",       DEF, "Deportivo Toluca",   30,  0.10, FIT,    3, false),
            p("Frantzdy Pierrot",     FWD, "Gent",               32,  0.10, FIT,    5, true),
            p("Duckens Nazon",        FWD, "Beerschot",          31,  0.10, FIT,    4, true),
            p("Kevin Fortune",        MID, "Lommel",             26,  0.10, FIT,    2, false)
        );
    }

    private static Squad curacao() {
        return squad("Curacao",
            p("Eloy Room",            GK,  "Orlando City",       33,  0.10, FIT,    3, true),
            p("Leandro Bacuna",       MID, "Middlesbrough",      32,  0.10, FIT,    5, false),
            p("Jurickson Schilder",   MID, "Heracles",           25,  0.20, FIT,    5, true),
            p("Juninho",              FWD, "Club Brugge",        30,  0.20, FIT,    8, true),
            p("Daishawn Redan",       FWD, "Cadiz",              24,  0.20, FIT,    5, false)
        );
    }

    // ── CAF ───────────────────────────────────────────────────────────────────────

    private static Squad morocco() {
        return squad("Morocco",
            p("Yassine Bounou",       GK,  "Al-Hilal",           33,  0.20, FIT,   10, true),
            p("Achraf Hakimi",        DEF, "PSG",                26,  0.60, FIT,   80, true),
            p("Nayef Aguerd",         DEF, "West Ham",           28,  0.20, FIT,   20, false),
            p("Sofyan Amrabat",       MID, "Fenerbahce",         28,  0.20, FIT,   20, true),
            p("Bilal El Khannouss",   MID, "Genk",               21,  0.40, FIT,   30, true),
            p("Hakim Ziyech",         FWD, "Galatasaray",        32,  0.10, FIT,    8, true),
            p("Youssef En-Nesyri",    FWD, "Fenerbahce",         27,  0.30, FIT,   30, true),
            p("Soufiane Rahimi",      FWD, "Al-Ain",             26,  0.40, FIT,   15, false)
        );
    }

    private static Squad senegal() {
        return squad("Senegal",
            p("Edouard Mendy",        GK,  "Al-Ahli",            32,  0.20, FIT,   12, true),
            p("Kalidou Koulibaly",    DEF, "Al-Hilal",           34,  0.20, FIT,   12, true),
            p("Idrissa Gueye",        MID, "Everton",            35,  0.10, FIT,    5, false),
            p("Sadio Mane",           FWD, "Al-Nassr",           34,  0.30, FIT,   15, true),
            p("Ismaila Sarr",         FWD, "Crystal Palace",     26,  0.30, FIT,   30, true),
            p("Nicolas Jackson",      FWD, "Chelsea",            23,  0.50, FIT,   55, true)
        );
    }

    private static Squad algeria() {
        return squad("Algeria",
            p("Rais M'Bolhi",         GK,  "ES Setif",           38,  0.00, FIT,    2, false),
            p("Youcef Atal",          DEF, "OGC Nice",           28,  0.30, FIT,   20, true),
            p("Rayan Ait-Nouri",      DEF, "Wolverhampton",      24,  0.40, FIT,   22, true),
            p("Ismail Bennacer",      MID, "AC Milan",           27,  0.50, FIT,   35, true),
            p("Adam Ounas",           MID, "Nottm Forest",       28,  0.30, FIT,   12, false),
            p("Riyad Mahrez",         FWD, "Al-Ahli",            35,  0.30, FIT,   15, true),
            p("Andy Delort",          FWD, "OGC Nice",           33,  0.10, FIT,    8, false)
        );
    }

    private static Squad ivoryCoast() {
        return squad("Ivory Coast",
            p("Yahia Fofana",         GK,  "Chelsea",            28,  0.30, FIT,   25, false),
            p("Simon Adingra",        FWD, "Brighton",           23,  0.40, FIT,   35, true),
            p("Franck Kessie",        MID, "Al-Ahli",            27,  0.20, FIT,   20, true),
            p("Ibrahim Sangare",      MID, "Nottm Forest",       26,  0.30, FIT,   35, true),
            p("Wilfried Zaha",        FWD, "Galatasaray",        31,  0.30, FIT,   12, true),
            p("Sebastien Haller",     FWD, "Dortmund",           30,  0.00, DOUBT, 30, true)
        );
    }

    private static Squad egypt() {
        return squad("Egypt",
            p("Mohamed El-Shenawy",   GK,  "Al-Ahly",            36,  0.10, FIT,    3, false),
            p("Ahmed Hegazi",         DEF, "Al-Ittihad",         33,  0.00, FIT,    3, false),
            p("Mohamed El-Nenni",     MID, "Galatasaray",        32,  0.20, FIT,    8, false),
            p("Mohamed Salah",        FWD, "Liverpool",          33,  0.70, FIT,   50, true),
            p("Omar Marmoush",        FWD, "Man City",           25,  0.60, FIT,   55, true),
            p("Mostafa Mohamed",      FWD, "Nantes",             27,  0.10, FIT,   10, false)
        );
    }

    private static Squad drCongo() {
        return squad("DR Congo",
            p("Joel Kiassumbua",      GK,  "Beerschot",          32,  0.10, FIT,    2, false),
            p("Chancel Mbemba",       DEF, "Marseille",          30,  0.20, FIT,   12, true),
            p("Arthur Masuaku",       DEF, "Besiktas",           31,  0.20, FIT,   10, false),
            p("Dodi Lukebakio",       FWD, "Sevilla",            27,  0.40, FIT,   20, true),
            p("Silas",                FWD, "Stuttgart",          26,  0.30, FIT,   15, true),
            p("Theo Bongonda",        FWD, "Trabzonspor",        29,  0.20, FIT,    8, false)
        );
    }

    private static Squad ghana() {
        return squad("Ghana",
            p("Lawrence Ati-Zigi",    GK,  "FC St. Gallen",      27,  0.20, FIT,    5, false),
            p("Mohammed Salisu",      DEF, "Southampton",        25,  0.20, FIT,   22, true),
            p("Thomas Partey",        MID, "Arsenal",            31,  0.20, DOUBT, 35, true),
            p("Antoine Semenyo",      FWD, "Bournemouth",        24,  0.40, FIT,   28, true),
            p("Inaki Williams",       FWD, "Athletic Bilbao",    30,  0.30, FIT,   25, true)
        );
    }

    private static Squad tunisia() {
        return squad("Tunisia",
            p("Aymen Dahmen",         GK,  "Montpellier",        28,  0.10, FIT,    5, false),
            p("Ellyes Skhiri",        MID, "Eintracht Frankfurt",28,  0.20, FIT,   20, true),
            p("Hannibal Mejbri",      MID, "Burnley",            21,  0.30, FIT,   18, true),
            p("Seifeddine Jaziri",    FWD, "Zamalek",            33,  0.00, FIT,    3, false)
        );
    }

    private static Squad capeVerde() {
        return squad("Cape Verde",
            p("Vozinha",              GK,  "Leixoes SC",         34,  0.10, FIT,    1, false),
            p("Steven Fortes",        DEF, "Empoli",             34,  0.10, FIT,    3, false),
            p("Ryan Mendes",          MID, "Vitoria Guimaraes",  34,  0.10, FIT,    3, false),
            p("Gilberto",             FWD, "Benfica",            31,  0.30, FIT,   10, true),
            p("Garry Rodrigues",      FWD, "Fatih Karagumruk",   32,  0.10, FIT,    5, false)
        );
    }

    private static Squad southAfrica() {
        return squad("South Africa",
            p("Ronwen Williams",      GK,  "Mamelodi Sundowns",  31,  0.20, FIT,    3, true),
            p("Bongani Zungu",        MID, "AmaZulu FC",         31,  0.10, FIT,    4, false),
            p("Themba Zwane",         MID, "Mamelodi Sundowns",  33,  0.10, FIT,    3, false),
            p("Percy Tau",            FWD, "Al-Ahly",            30,  0.20, FIT,   10, true),
            p("Evidence Makgopa",     FWD, "Orlando Pirates",    24,  0.20, FIT,    5, false)
        );
    }

    // ── AFC ───────────────────────────────────────────────────────────────────────

    private static Squad japan() {
        return squad("Japan",
            p("Shuichi Gonda",        GK,  "Shimizu S-Pulse",    34,  0.00, FIT,    1, false),
            p("Hiroki Ito",           DEF, "Stuttgart",          25,  0.40, FIT,   28, true),
            p("Wataru Endo",          MID, "Liverpool",          32,  0.20, FIT,   15, true),
            p("Ritsu Doan",           MID, "Freiburg",           26,  0.40, FIT,   22, true),
            p("Kaoru Mitoma",         FWD, "Brighton",           27,  0.60, FIT,   55, true),
            p("Takefusa Kubo",        FWD, "Real Sociedad",      23,  0.70, FIT,   55, true),
            p("Ayase Ueda",           FWD, "Feyenoord",          26,  0.40, FIT,   18, false)
        );
    }

    private static Squad southKorea() {
        return squad("South Korea",
            p("Jo Hyeon-woo",         GK,  "Ulsan Hyundai",      33,  0.10, FIT,    3, false),
            p("Kim Min-jae",          DEF, "Bayern Munich",      28,  0.50, FIT,   65, true),
            p("Lee Kang-in",          MID, "PSG",                23,  0.50, FIT,   45, true),
            p("Son Heung-min",        FWD, "Tottenham",          34,  0.50, FIT,   30, true),
            p("Hwang Hee-chan",        FWD, "Wolverhampton",      28,  0.40, FIT,   28, true)
        );
    }

    private static Squad australia() {
        return squad("Australia",
            p("Mathew Ryan",          GK,  "Real Sociedad",      32,  0.20, FIT,   10, true),
            p("Harry Souttar",        DEF, "Leicester",          26,  0.10, FIT,   18, false),
            p("Jackson Irvine",       MID, "FC St. Pauli",       31,  0.20, FIT,   10, false),
            p("Martin Boyle",         FWD, "Al-Faisaly",         31,  0.10, FIT,    5, false),
            p("Mitchell Duke",        FWD, "Fagiano Okayama",    33,  0.10, FIT,    3, false),
            p("Marco Tilio",          FWD, "Celtic",             23,  0.20, FIT,   12, false)
        );
    }

    private static Squad saudiArabia() {
        return squad("Saudi Arabia",
            p("Mohammed Al-Owais",    GK,  "Al-Hilal",           32,  0.20, FIT,    8, true),
            p("Ali Al-Bulayhi",       DEF, "Al-Hilal",           34,  0.00, FIT,    3, false),
            p("Salem Al-Dawsari",     FWD, "Al-Hilal",           32,  0.20, FIT,   10, true),
            p("Firas Al-Buraikan",    FWD, "Al-Fateh",           23,  0.20, FIT,    8, false)
        );
    }

    private static Squad iran() {
        return squad("Iran",
            p("Alireza Beiranvand",   GK,  "Persepolis",         31,  0.20, FIT,    5, true),
            p("Sadegh Moharrami",     DEF, "Feyenoord",          28,  0.20, FIT,   10, false),
            p("Mehdi Taremi",         FWD, "Inter Milan",        32,  0.40, FIT,   35, true),
            p("Sardar Azmoun",        FWD, "Bayer Leverkusen",   29,  0.30, FIT,   22, true)
        );
    }

    private static Squad uzbekistan() {
        return squad("Uzbekistan",
            p("Utkir Yusupov",        GK,  "Pakhtakor",          30,  0.10, FIT,    1, false),
            p("Eldor Shomurodov",     FWD, "AS Roma",            27,  0.30, FIT,   18, true),
            p("Abbosbek Fayzullaev",  MID, "CSKA Moscow",        22,  0.30, FIT,   12, true)
        );
    }

    private static Squad iraq() {
        return squad("Iraq",
            p("Jalal Hassan",         GK,  "Al-Shorta",          34,  0.00, FIT,    1, false),
            p("Ali Adnan",            DEF, "Deportivo La Coruna",31,  0.10, FIT,    5, false),
            p("Aymen Hussein",        FWD, "Al-Quwa Al-Jawiya",  27,  0.20, FIT,    5, true)
        );
    }

    private static Squad qatar() {
        return squad("Qatar",
            p("Meshaal Barsham",      GK,  "Al-Sadd",            26,  0.10, FIT,    3, false),
            p("Karim Boudiaf",        MID, "Al-Duhail",          32,  0.00, FIT,    4, false),
            p("Akram Afif",           FWD, "Al-Sadd",            27,  0.30, FIT,   12, true),
            p("Almoez Ali",           FWD, "Al-Duhail",          28,  0.20, FIT,    8, true)
        );
    }

    private static Squad jordan() {
        return squad("Jordan",
            p("Yazeed Abo-Laila",     GK,  "Al-Jazeera",         28,  0.10, FIT,    1, false),
            p("Bachar Bani Yaseen",   DEF, "Al-Wihdat",          28,  0.10, FIT,    2, false),
            p("Moussa Al-Taamari",    FWD, "Montpellier",        25,  0.30, FIT,    8, true),
            p("Ahmad Sholi",          FWD, "Al-Qadsia",          28,  0.10, FIT,    3, false)
        );
    }

    // ── OFC ───────────────────────────────────────────────────────────────────────

    private static Squad newZealand() {
        return squad("New Zealand",
            p("Max Crocombe",         GK,  "SD Huesca",          29,  0.10, FIT,    3, false),
            p("Liberato Cacace",      DEF, "Empoli",             23,  0.20, FIT,    8, true),
            p("Chris Wood",           FWD, "Nottm Forest",       32,  0.20, FIT,   18, true)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private static Player p(String name, Player.Position pos, String club, int age,
                             double form, Player.Fitness fitness, int valueM, boolean isStar) {
        return new Player(name, pos, club, age, form, fitness, valueM, isStar);
    }

    private static Squad squad(String teamName, Player... players) {
        return new Squad(teamName, Arrays.asList(players));
    }
}