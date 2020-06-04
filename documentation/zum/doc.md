## Crawler - chodící robot pomocí HyperNEAT

### Jan Bouček, FIT ČVUT

Upozornění: tato dokumentace je z velké části pouze zkrácená, aktualizovaná a pro BI-ZUM upravená verze původní dokumentace ([zde](https://github.com/Colanderr/Crawler/blob/master/documentation/crawler.pdf)) odevzdané jako součást maturitní práce, která detailněji popisuje jednotlivé využité techniky. 

### Cíl projektu

Cílem projektu je vyvinout ovladač simulovaného čtyřnohého 2D robota, který dosahuje co nejrychlejší plynulé chůze v jednom směru, pomocí HyperNEAT.

Chůzi lze definovat jakožto sérii stavů kráčivého tělesa, kde každý stav lze zjednodušit jako soubor informací popisujících jednotlivé pohyblivé prvky robota. V rámci tohoto projektu budeme pracovat s dvojrozměrnou simulací čtyřnohého robota, jehož každá noha je ovládána dvěma klouby. Stav robota tak lze vystihnout jako osmirozměrný vektor $\vec{s}$. Hledáme pak ovladač robota, který pro každý stav malezne osmirozměrný vektor $\vec{\omega}$, ten každému kloubu určuje úhlovou rychlost.

### Využitá teorie



#### NEAT

*Neuroevolution of augmenting topologies* ([ref](http://www.mitpressjournals.org/doi/10.1162/106365602320169811)) - NEAT - je metodou, která definuje jeden ze způsobů, kterými lze vyvíjet neuronové sítě pomocí evolučního algoritmu. Ideou tohoto systému je způsob, kterým zapisuje neuronové sítě, jakožto *živočichy* v genetickém algoritmu.
Tento systém každou neuronovou síť popisuje jako ohodnocený graf pomocí dvou seznamů *genů* - seznamu vrcholů a seznamu hran. Každému vrcholu a hraně přisuzuje číselné identifikátory, pomocí kterých dokáže udržovat přehled o tom, kteří jedinci jsou si *geneticky* podobní.
Stanley a Miikkulainen  díky tomu zavádí i proces *speciace*, který ještě před rozmnožováním roztřídí jedince na různé *druhy* podle příbuznosti tak, aby se spolu křížily jen sítě s menšími rozdíly. Každý druh je pak ohodnocen svojí průměrnou hodnotou fitness, pomocí které se určí počet potomků v další generaci daného druhu.
Při procesu rozmnožování je z každého druhu vybrána *silnější* část, ze které se vytvoří požadovaný počet potomků, z nichž každý může vzniknout křížením - většinu genů zdědí po silnějším rodiči, ale část od slabšího, nebo bez křížení, kdy se jedinec pouze zkopíruje do další generace.
Pak na všech potomcích proběhnou mutace, přičemž je náhodně rozhodnuto, které z druhů mutací na nich proběhnou, Stanley a Miikkulainen jich popisují hned několik:

- přidání nové hrany do sítě
- rozdělení hrany na dvě hrany s novým neuronem uprostřed
- změna všech vah o~malou hodnotu, nebo na náhodnou hodnotu

Po zmutování jsou všichni potomci prohlášeni za současnou generaci a algoritmus pokračuje znovu hodnocením jedinců.

#### CPPN-NEAT

Tím, že v NEAT algoritmu umožníme každému neuronu využívat jinou aktivační funkci, můžeme dosáhnout tvorby sítí, které jsou dobře uzpůsobené ke generování fyzické geometrie živočichů. Metoda CPPN-NEAT ([ref](http://link.springer.com/10.1007/s10710-007-9028-8)) využívá různých vlastností funkcí - symetrie, repetice apod. tak, že průchodem celou sítí jsou výsledná data nakombinována pomocí komplexní složené funkce, která si uchováva všechny tyto vlastnosti.
Pokud například vytvoříme CPPN síť s dvěma vstupy - souřadnicemi $x$ a $y$ s jedním výstupem, získáme dvojrozměrné útvary (viz obr. níže), které mají velmi blízko k fyzickému rozložení v reálných živočiších, např. dokáže nakombinovat repetici s variací a generovat útvary podobné prstům ruky nebo pomocí symetrie a variace útvar podobný lidskému oku (viz obr. níže).
V samotném algoritmu stačí jen při tvoření nových neuronů určit náhodnou aktivační funkci a přidat mutační operátor, který změní funkci u náhodného neuronu.



![cppn_merged](../images/cppn_merged.jpg)

#### HyperNEAT

Technika HyperNEAT ([ref](http://www.mitpressjournals.org/doi/10.1162/artl.2009.15.2.15202)) využívá CPPN sítě ke generování čtyřrozměrného prostoru, který ve výsledku slouží jako definice vah v další neuronové síti.  Tzn. "organická struktura", kterou generujeme pomocí CPPN-NEAT je *mozek*.
To znamená, že generujeme CPPN síť, která má čtyři vstupy - $x_1$, $y_1$, $x_2$, $y_2$. Výstup nám pak určuje váhu spoje z neuronu na *fyzických* souřadnicích $[x_1;y_1]$ do neuronu na souřadnicích $[x_2;y_2]$. Stačí nám pak navrhnout *fyzickou* strukturu sítě k využití této techniky.
V tomto projektu je použito rozložení zvané *state-space sandwich* ([ref](http://ieeexplore.ieee.org/document/4983289/)) o třech vrstvách podobně jako v referovaném článku, kde výsledná síť je rozvrstvená do trojrozměrného prostoru a z každého neuronu vede spoj do každého neuronu v další vrstvě. CPPN síť má pak dva výstupy, kde první určuje váhu mezi souřadnicemi $[x_1;y_1]$ z první vrstvy do $[x_2;y_2]$ v druhé a druhý výstup určuje stejným způsobem váhy mezi druhou a třetí vrstvou.

### Implementace

Projekt je implementován v jazyce Kotlin (původní verze byla v Javě), byl vybrán zejména pro dobrou objektovou strukturu a protože se mi v něm programuje nejpohodlněji. V následním textu je popsáno, jak jednotlivé komponenty projektu fungují a jaké všechny vylepšení jsem vystavěl na základním algoritmu.

#### NEAT

NEAT je impementován V tomto projektu je použito ještě několik dalších mutačních operátorů:

- aktivace/deaktivace jedné hrany
- změna jedné váhy na náhodnou hodnotu
- změna jedné váhy o~maximálně $\pm 5\%$

Díky této malé úpravě dokáže algoritmus ladit neuronovou síť v mírně detailnějším měřítku. 