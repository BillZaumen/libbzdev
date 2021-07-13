import org.bzdev.math.stats.*;

public class ChiSquareTest {

    // http://keisan.casio.com/exec/system/1180573195
    // has a calculator that provided the following tables
    // of test data.

    static double[][] tableP2 = {
	{0.1,0.0487705755},
	{0.3, 0.1392920236},
	{0.5,	0.2211992169},
	{0.7,	0.2953119103},
	{0.9,	0.3623718484},
	{1.1,	0.4230501896},
	{1.3,	0.4779542232},
	{1.5,	0.5276334473},
	{1.7,	0.5725850681},
	{1.9,	0.6132589765},
	{2.1,	0.6500622509},
	{2.3,	0.6833632306},
	{2.5,	0.7134952031},
	{2.7,	0.7407597394},
	{2.9,	0.7654297119},
	{3.1,	0.7877520262},
	{3.3,	0.807950091},
	{3.5,	0.8262260565},
	{3.7,	0.8427628337},
	{3.9,	0.8577259284},
	{4.1,	0.8712650964},
	{4.3,	0.8835158422},
	{4.5,	0.8946007754},
	{4.7,	0.9046308378},
	{4.9,	0.9137064135},
	{5.1,	0.921918334},
	{5.3,	0.9293487869},
	{5.5,	0.9360721388},
	{5.7,	0.9421556791},
	{5.9,	0.9476602941},
	{6.1,	0.9526410756},
	{6.3,	0.9571478731},
	{6.5,	0.9612257922},
	{6.7,	0.9649156459},
	{6.9,	0.9682543636},
	{7.1,	0.9712753603},
	{7.3,	0.9740088712},
	{7.5,	0.9764822541},
	{7.7,	0.9787202636},
	{7.9,	0.9807452982},
	{8.1,	0.9825776254},
	{8.3,	0.9842355835},
	{8.5,	0.9857357661},
	{8.7,	0.9870931874},
	{8.9,	0.988321433},
	{9.1,	0.9894327956},
	{9.3,	0.9904383981},
	{9.5,	0.9913483048},
	{9.7,	0.9921716225},
	{9.9,	0.9929165911},
	{10.1,	0.9935906666},
	{10.3,	0.9942005953},
	{10.5,	0.9947524816},
	{10.7,	0.995251849},
	{10.9,	0.9957036953},
	{11.1,	0.9961125428},
	{11.3,	0.9964824832},
	{11.5,	0.9968172192},
	{11.7,	0.9971201008},
	{11.9,	0.9973941595},
	{12.1,	0.997642138},
	{12.3,	0.9978665182},
	{12.5,	0.9980695459},
	{12.7,	0.9982532529},
	{12.9,	0.9984194778},
	{13.1,	0.9985698844},
	{13.3,	0.9987059779},
	{13.5,	0.9988291204},
	{13.7,	0.9989405443},
	{13.9,	0.9990413648},
	{14.1,	0.999132591},
	{14.3,	0.9992151359},
	{14.5,	0.9992898256},
	{14.7,	0.9993574076},
	{14.9,	0.9994185584},
	{15.1,	0.9994738899},
	{15.3,	0.9995239559},
	{15.5,	0.9995692575},
	{15.7,	0.999610248},
	{15.9,	0.9996473378},
	{16.1,	0.9996808981},
	{16.3,	0.9997112646},
	{16.5,	0.9997387414},
	{16.7,	0.9997636035},
	{16.9,	0.9997860996},
	{17.1,	0.9998064549},
	{17.3,	0.9998248732},
	{17.5,	0.9998415387},
	{17.7,	0.9998566183},
	{17.9,	0.9998702628},
	{18.1,	0.999882609},
	{18.3,	0.9998937802},
	{18.5,	0.9999038883},
	{18.7,	0.9999130346},
	{18.9,	0.9999213104},
	{19.1,	0.9999287987},
	{19.3,	0.9999355744},
	{19.5,	0.9999417053},
	{19.7,	0.9999472528},
	{19.9,	0.9999522724},
	{20.1,	0.9999568143}};

    static double[][] tableP5 = {
	{0.1,	1.623166119E-4},
	{0.3,	0.002356913739},
	{0.5,	0.00787670677},
	{0.7,	0.01703132481},
	{0.9,	0.02977836159},
	{1.1,	0.04589632397},
	{1.3,	0.06506832091},
	{1.5,	0.0869301855},
	{1.7,	0.1111002405},
	{1.9,	0.1371982774},
	{2.1,	0.1648576994},
	{2.3,	0.1937331301},
	{2.5,	0.2235049289},
	{2.7,	0.2538815625},
	{2.9,	0.2846004857},
	{3.1,	0.3154279853},
	{3.3,	0.3461583176},
	{3.5,	0.3766123723},
	{3.7,	0.4066360382},
	{3.9,	0.4360983958},
	{4.1,	0.4648898276},
	{4.3,	0.4929201141},
	{4.5,	0.5201165619},
	{4.7,	0.5464221983},
	{4.9,	0.571794053},
	{5.1,	0.5962015429},
	{5.3,	0.619624968},
	{5.5,	0.642054119},
	{5.7,	0.663487006},
	{5.9,	0.683928691},
	{6.1,	0.7033902354},
	{6.3,	0.721887751},
	{6.5,	0.739441544},
	{6.7,	0.756075357},
	{6.9,	0.7718156919},
	{7.1,	0.7866912101},
	{7.3,	0.8007322101},
	{7.5,	0.8139701664},
	{7.7,	0.8264373298},
	{7.9,	0.8381663816},
	{8.1,	0.8491901371},
	{8.3,	0.8595412924},
	{8.5,	0.8692522107},
	{8.7,	0.8783547435},
	{8.9,	0.8868800828},
	{9.1,	0.894858641},
	{9.3,	0.9023199544},
	{9.5,	0.9092926083},
	{9.7,	0.9158041809},
	{9.9,	0.9218812023},
	{10.1,	0.9275491287},
	{10.3,	0.9328323272},
	{10.5,	0.9377540719},
	{10.7,	0.942336548},
	{10.9,	0.9466008633},
	{11.1,	0.9505670661},
	{11.3,	0.9542541674},
	{11.5,	0.9576801679},
	{11.7,	0.9608620874},
	{11.9,	0.9638159971},
	{12.1,	0.9665570538},
	{12.3,	0.9690995354},
	{12.5,	0.9714568767},
	{12.7,	0.9736417064},
	{12.9,	0.9756658835},
	{13.1,	0.9775405341},
	{13.3,	0.9792760874},
	{13.5,	0.9808823107},
	{13.7,	0.9823683446},
	{13.9,	0.9837427367},
	{14.1,	0.9850134736},
	{14.3,	0.9861880133},
	{14.5,	0.9872733149},
	{14.7,	0.9882758682},
	{14.9,	0.9892017218},
	{15.1,	0.9900565091},
	{15.3,	0.9908454745},
	{15.5,	0.9915734975},
	{15.7,	0.9922451155},
	{15.9,	0.9928645458},
	{16.1,	0.9934357063},
	{16.3,	0.993962235},
	{16.5,	0.9944475085},
	{16.7,	0.9948946595},
	{16.9,	0.9953065932},
	{17.1,	0.9956860026},
	{17.3,	0.996035383},
	{17.5,	0.996357046},
	{17.7,	0.9966531316},
	{17.9,	0.996925621},
	{18.1,	0.9971763471},
	{18.3,	0.9974070054},
	{18.5,	0.9976191636},
	{18.7,	0.997814271},
	{18.9,	0.9979936667},
	{19.1,	0.9981585881},
	{19.3,	0.9983101777},
	{19.5,	0.9984494907},
	{19.7,	0.9985775007},
	{19.9,	0.9986951065},
	{20.1,	0.9988031371}};

    static double[][]tableP6 = {
	{0.1,	2.00674936E-5},
	{0.3,	5.02862376E-4},
	{0.5,	0.00216149669},
	{0.7,	0.00550893338},
	{0.9,	0.0108793298},
	{1.1,	0.0184641351},
	{1.3,	0.028342298},
	{1.5,	0.0405054397},
	{1.7,	0.0548787317},
	{1.9,	0.0713381174},
	{2.1,	0.0897244301},
	{2.3,	0.1098548821},
	{2.5,	0.1315323345},
	{2.7,	0.1545527},
	{2.9,	0.1787107788},
	{3.1,	0.203804788},
	{3.3,	0.229639804},
	{3.5,	0.2560303046},
	{3.7,	0.282801975},
	{3.9,	0.30979291},
	{4.1,	0.336854328},
	{4.3,	0.363850893},
	{4.5,	0.390660733},
	{4.7,	0.417175207},
	{4.9,	0.4432985},
	{5.1,	0.468947069},
	{5.3,	0.494049},
	{5.5,	0.5185432953},
	{5.7,	0.542379116},
	{5.9,	0.565515016},
	{6.1,	0.587918159},
	{6.3,	0.609563559},
	{6.5,	0.6304333316},
	{6.7,	0.650515978},
	{6.9,	0.6698057},
	{7.1,	0.688301754},
	{7.3,	0.7060078446},
	{7.5,	0.7229315566},
	{7.7,	0.739083832},
	{7.9,	0.754478484},
	{8.1,	0.7691317581},
	{8.3,	0.7830619236},
	{8.5,	0.7962889095},
	{8.7,	0.8088339722},
	{8.9,	0.8207193988},
	{9.1,	0.8319682413},
	{9.3,	0.8426040802},
	{9.5,	0.8526508161},
	{9.7,	0.8621324859},
	{9.9,	0.8710731032},
	{10.1,	0.8794965195},
	{10.3,	0.887426305},
	{10.5,	0.8948856471},
	{10.7,	0.9018972652},
	{10.9,	0.9084833397},
	{11.1,	0.9146654542},
	{11.3,	0.9204645488},
	{11.5,	0.9259008846},
	{11.7,	0.9309940163},
	{11.9,	0.9357627739},
	{12.1,	0.9402252508},
	{12.3,	0.9443987982},
	{12.5,	0.9483000252},
	{12.7,	0.9519448028},
	{12.9,	0.9553482731},
	{13.1,	0.95852486},
	{13.3,	0.9614882846},
	{13.5,	0.9642515816},
	{13.7,	0.9668271179},
	{13.9,	0.9692266133},
	{14.1,	0.971461161},
	{14.3,	0.9735412507},
	{14.5,	0.9754767906},
	{14.7,	0.9772771309},
	{14.9,	0.9789510868},
	{15.1,	0.9805069622},
	{15.3,	0.981952572},
	{15.5,	0.9832952658},
	{15.7,	0.9845419497},
	{15.9,	0.9856991084},
	{16.1,	0.9867728264},
	{16.3,	0.9877688092},
	{16.5,	0.9886924031},
	{16.7,	0.9895486144},
	{16.9,	0.9903421289},
	{17.1,	0.991077329},
	{17.3,	0.9917583116},
	{17.5,	0.9923889045},
	{17.7,	0.9929726819},
	{17.9,	0.9935129798},
	{18.1,	0.9940129104},
	{18.3,	0.9944753753},
	{18.5,	0.994903079},
	{18.7,	0.9952985407},
	{18.9,	0.9956641066},
	{19.1,	0.9960019601},
	{19.3,	0.9963141328},
	{19.5,	0.9966025141},
	{19.7,	0.9968688607},
	{19.9,	0.9971148049},
	{20.1,	0.9973418632}};

    static double[][] tableQ2 = {
	{0.1,	0.9512294245},
	{0.3,	0.8607079764},
	{0.5,	0.7788007831},
	{0.7,	0.7046880897},
	{0.9,	0.6376281516},
	{1.1,	0.5769498104},
	{1.3,	0.5220457768},
	{1.5,	0.4723665527},
	{1.7,	0.4274149319},
	{1.9,	0.3867410235},
	{2.1,	0.3499377491},
	{2.3,	0.3166367694},
	{2.5,	0.2865047969},
	{2.7,	0.2592402606},
	{2.9,	0.2345702881},
	{3.1,	0.2122479738},
	{3.3,	0.192049909},
	{3.5,	0.1737739435},
	{3.7,	0.1572371663},
	{3.9,	0.1422740716},
	{4.1,	0.1287349036},
	{4.3,	0.1164841578},
	{4.5,	0.1053992246},
	{4.7,	0.09536916222},
	{4.9,	0.0862935865},
	{5.1,	0.078081666},
	{5.3,	0.07065121306},
	{5.5,	0.06392786121},
	{5.7,	0.05784432087},
	{5.9,	0.0523397059},
	{6.1,	0.04735892439},
	{6.3,	0.04285212687},
	{6.5,	0.03877420783},
	{6.7,	0.0350843541},
	{6.9,	0.03174563638},
	{7.1,	0.02872463965},
	{7.3,	0.02599112878},
	{7.5,	0.02351774586},
	{7.7,	0.02127973644},
	{7.9,	0.01925470178},
	{8.1,	0.01742237464},
	{8.3,	0.01576441648},
	{8.5,	0.01426423391},
	{8.7,	0.01290681258},
	{8.9,	0.01167856697},
	{9.1,	0.01056720438},
	{9.3,	0.00956160193},
	{9.5,	0.008651695203},
	{9.7,	0.007828377549},
	{9.9,	0.00708340893},
	{10.1,	0.006409333446},
	{10.3,	0.005799404727},
	{10.5,	0.005247518399},
	{10.7,	0.004748150999},
	{10.9,	0.004296304691},
	{11.1,	0.003887457243},
	{11.3,	0.003517516775},
	{11.5,	0.003182780797},
	{11.7,	0.002879899158},
	{11.9,	0.002605840518},
	{12.1,	0.002357862006},
	{12.3,	0.00213348177},
	{12.5,	0.001930454136},
	{12.7,	0.001746747136},
	{12.9,	0.001580522169},
	{13.1,	0.001430115598},
	{13.3,	0.001294022105},
	{13.5,	0.001170879621},
	{13.7,	0.001059455693},
	{13.9,	9.58635154E-4},
	{14.1,	8.67408957E-4},
	{14.3,	7.848640813E-4},
	{14.5,	7.101743888E-4},
	{14.7,	6.425923604E-4},
	{14.9,	5.814416122E-4},
	{15.1,	5.261101271E-4},
	{15.3,	4.76044129E-4},
	{15.5,	4.307425406E-4},
	{15.7,	3.897519683E-4},
	{15.9,	3.526621646E-4},
	{16.1,	3.191019225E-4},
	{16.3,	2.887353596E-4},
	{16.5,	2.612585573E-4},
	{16.7,	2.363965184E-4},
	{16.9,	2.139004154E-4},
	{17.1,	1.935450996E-4},
	{17.3,	1.751268482E-4},
	{17.5,	1.584613251E-4},
	{17.7,	1.433817363E-4},
	{17.9,	1.2973716E-4},
	{18.1,	1.173910369E-4},
	{18.3,	1.062198027E-4},
	{18.5,	9.61116521E-5},
	{18.7,	8.696541909E-5},
	{18.9,	7.86895653E-5},
	{19.1,	7.120126307E-5},
	{19.3,	6.4425567E-5},
	{19.5,	5.829466373E-5},
	{19.7,	5.274719302E-5},
	{19.9,	4.772763394E-5},
	{20.1,	4.318574906E-5}};

    static double[][] tableQ5 = {
	{0.1,	0.9998376834},
	{0.3,	0.9976430863},
	{0.5,	0.9921232932},
	{0.7,	0.9829686752},
	{0.9,	0.9702216384},
	{1.1,	0.954103676},
	{1.3,	0.9349316791},
	{1.5,	0.9130698145},
	{1.7,	0.8888997595},
	{1.9,	0.8628017226},
	{2.1,	0.8351423006},
	{2.3,	0.8062668699},
	{2.5,	0.7764950711},
	{2.7,	0.7461184375},
	{2.9,	0.7153995143},
	{3.1,	0.6845720147},
	{3.3,	0.6538416824},
	{3.5,	0.6233876277},
	{3.7,	0.5933639618},
	{3.9,	0.5639016042},
	{4.1,	0.5351101724},
	{4.3,	0.5070798859},
	{4.5,	0.4798834381},
	{4.7,	0.4535778017},
	{4.9,	0.428205947},
	{5.1,	0.4037984571},
	{5.3,	0.380375032},
	{5.5,	0.357945881},
	{5.7,	0.336512994},
	{5.9,	0.316071309},
	{6.1,	0.2966097646},
	{6.3,	0.278112249},
	{6.5,	0.260558456},
	{6.7,	0.243924643},
	{6.9,	0.2281843081},
	{7.1,	0.2133087899},
	{7.3,	0.1992677899},
	{7.5,	0.1860298336},
	{7.7,	0.1735626702},
	{7.9,	0.1618336184},
	{8.1,	0.1508098629},
	{8.3,	0.1404587076},
	{8.5,	0.1307477893},
	{8.7,	0.1216452565},
	{8.9,	0.1131199172},
	{9.1,	0.105141359},
	{9.3,	0.0976800456},
	{9.5,	0.0907073917},
	{9.7,	0.0841958191},
	{9.9,	0.0781187977},
	{10.1,	0.0724508713},
	{10.3,	0.06716767277},
	{10.5,	0.0622459281},
	{10.7,	0.057663452},
	{10.9,	0.05339913674},
	{11.1,	0.0494329339},
	{11.3,	0.0457458326},
	{11.5,	0.0423198321},
	{11.7,	0.0391379126},
	{11.9,	0.0361840029},
	{12.1,	0.03344294621},
	{12.3,	0.0309004646},
	{12.5,	0.0285431233},
	{12.7,	0.0263582936},
	{12.9,	0.0243341165},
	{13.1,	0.02245946586},
	{13.3,	0.02072391264},
	{13.5,	0.01911768934},
	{13.7,	0.01763165535},
	{13.9,	0.01625726331},
	{14.1,	0.01498652637},
	{14.3,	0.01381198672},
	{14.5,	0.01272668512},
	{14.7,	0.01172413175},
	{14.9,	0.01079827823},
	{15.1,	0.00994349094},
	{15.3,	0.00915452547},
	{15.5,	0.00842650245},
	{15.7,	0.00775488445},
	{15.9,	0.00713545417},
	{16.1,	0.006564293706},
	{16.3,	0.00603776504},
	{16.5,	0.00555249152},
	{16.7,	0.00510534048},
	{16.9,	0.00469340677},
	{17.1,	0.0043139974},
	{17.3,	0.00396461696},
	{17.5,	0.00364295402},
	{17.7,	0.003346868357},
	{17.9,	0.00307437897},
	{18.1,	0.002823652881},
	{18.3,	0.002592994609},
	{18.5,	0.002380836395},
	{18.7,	0.00218572902},
	{18.9,	0.002006333257},
	{19.1,	0.001841411886},
	{19.3,	0.001689822255},
	{19.5,	0.001550509349},
	{19.7,	0.001422499332},
	{19.9,	0.001304893538},
	{20.1,	0.001196862885}};

    static double[][] tableQ6 = {
	{0.1,	0.9999799325},
	{0.3,	0.9994971376},
	{0.5,	0.9978385033},
	{0.7,	0.9944910666},
	{0.9,	0.9891206702},
	{1.1,	0.9815358649},
	{1.3,	0.971657702},
	{1.5,	0.9594945603},
	{1.7,	0.9451212683},
	{1.9,	0.9286618826},
	{2.1,	0.9102755699},
	{2.3,	0.8901451179},
	{2.5,	0.8684676655},
	{2.7,	0.8454473},
	{2.9,	0.8212892212},
	{3.1,	0.796195212},
	{3.3,	0.770360196},
	{3.5,	0.7439696954},
	{3.7,	0.717198025},
	{3.9,	0.69020709},
	{4.1,	0.663145672},
	{4.3,	0.636149107},
	{4.5,	0.609339267},
	{4.7,	0.582824793},
	{4.9,	0.5567015},
	{5.1,	0.531052931},
	{5.3,	0.505951},
	{5.5,	0.4814567047},
	{5.7,	0.457620884},
	{5.9,	0.434484984},
	{6.1,	0.412081841},
	{6.3,	0.390436441},
	{6.5,	0.3695666684},
	{6.7,	0.349484022},
	{6.9,	0.3301943},
	{7.1,	0.311698246},
	{7.3,	0.2939921554},
	{7.5,	0.2770684434},
	{7.7,	0.260916168},
	{7.9,	0.245521516},
	{8.1,	0.2308682419},
	{8.3,	0.2169380764},
	{8.5,	0.2037110905},
	{8.7,	0.1911660278},
	{8.9,	0.1792806012},
	{9.1,	0.1680317587},
	{9.3,	0.1573959198},
	{9.5,	0.1473491839},
	{9.7,	0.1378675141},
	{9.9,	0.1289268968},
	{10.1,	0.1205034805},
	{10.3,	0.112573695},
	{10.5,	0.1051143529},
	{10.7,	0.0981027348},
	{10.9,	0.0915166603},
	{11.1,	0.08533454582},
	{11.3,	0.0795354512},
	{11.5,	0.07409911542},
	{11.7,	0.0690059837},
	{11.9,	0.06423722608},
	{12.1,	0.0597747492},
	{12.3,	0.05560120178},
	{12.5,	0.0516999748},
	{12.7,	0.0480551972},
	{12.9,	0.04465172692},
	{13.1,	0.04147514},
	{13.3,	0.0385117154},
	{13.5,	0.03574841842},
	{13.7,	0.0331728821},
	{13.9,	0.03077338673},
	{14.1,	0.02853883896},
	{14.3,	0.02645874926},
	{14.5,	0.0245232094},
	{14.7,	0.0227228691},
	{14.9,	0.02104891316},
	{15.1,	0.01949303785},
	{15.3,	0.01804742799},
	{15.5,	0.01670473415},
	{15.7,	0.01545805025},
	{15.9,	0.0143008916},
	{16.1,	0.01322717356},
	{16.3,	0.01223119075},
	{16.5,	0.01130759693},
	{16.7,	0.01045138558},
	{16.9,	0.009657871129},
	{17.1,	0.00892267102},
	{17.3,	0.008241688383},
	{17.5,	0.007611095522},
	{17.7,	0.00702731812},
	{17.9,	0.00648702017},
	{18.1,	0.00598708962},
	{18.3,	0.00552462472},
	{18.5,	0.00509692105},
	{18.7,	0.00470145926},
	{18.9,	0.004335893408},
	{19.1,	0.00399803992},
	{19.3,	0.003685867222},
	{19.5,	0.00339748587},
	{19.7,	0.003131139311},
	{19.9,	0.002885195131},
	{20.1,	0.00265813684}};



    public static void main(String argv[]) throws Exception {

	// test against known values obtained using an on-line caculator

	if (Math.abs(ChiSquareDistr.P(0.2, 1) - 0.345279154) > 1.e-8) {
	    System.out.println(ChiSquareDistr.P(0.1, 2));
	    throw new Exception("CDF failed");
	}

	if (Math.abs(ChiSquareDistr.Q(0.2, 1) - 0.654720846) > 1.e-8) {
	    System.out.println(ChiSquareDistr.P(0.1, 2));
	    throw new Exception("1-CDF failed");
	}


	if (Math.abs(ChiSquareDistr.P(0.1, 2) - 0.048771) > 1.e-5) {
	    System.out.println(ChiSquareDistr.P(0.1, 2));
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(0.1,3) - 0.008163 ) > 1.e-5) {
	    System.out.println(ChiSquareDistr.P(0.1, 3));
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(0.1,4) - 0.001209 ) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	
	if (Math.abs(ChiSquareDistr.P(0.4, 2) - 0.181269) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(0.4,3) - 0.059758) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(0.4,4) - 0.017523) > 1.e-5) {
	    throw new Exception("CDF failed");
	}


	if (Math.abs(ChiSquareDistr.P(2.0, 2) - 0.632121) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(2.0,3) - 0.427593) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(2.0,4) - 0.264241) > 1.e-5) {
	    throw new Exception("CDF failed");
	}

	if (Math.abs(ChiSquareDistr.P(20.0, 2) - 0.999955) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(20.0,3) - 0.999830) > 1.e-5) {
	    throw new Exception("CDF failed");
	}
	if (Math.abs(ChiSquareDistr.P(20.0,4) - 0.999501) > 1.e-5) {
	    throw new Exception("CDF failed");
	}

	//Cross check PDF against numerical derivative of P.
	double delta = 0.00001;
	for (int i = 1; i < 10; i++) {
	    double x = i/1.0;
	    for (int d = 0; d < 6; d++) {
		double value = ChiSquareDistr.pd(x, d);
		double deriv =
		    (ChiSquareDistr.P(x+delta, d)-ChiSquareDistr.P(x,d))/delta;
		if (Math.abs(value - deriv) > 1.e-5) {
		    System.out.format("%s != %s\n", value, deriv);
		    throw new Exception("PDF failed");
		}
	    }
	}

	int errcount = 0;

	double limit = 1.e-9;
	int nu = 2;
	for (double[] data: tableP2) {
	    double x = data[0];
	    double value = ChiSquareDistr.P(x, nu);
	    double expected = data[1];
	    if (Math.abs((value - expected)/expected) > limit) {
		System.out.format("P(%g,%d) = %s, expecting %s\n",
				  x, nu, value, expected);
		errcount++;
	    }
	}

	nu = 5;
	for (double[] data: tableP5) {
	    double x = data[0];
	    double value = ChiSquareDistr.P(x, nu);
	    double expected = data[1];
	    if (Math.abs((value - expected)/expected) > limit) {
		System.out.format("P(%g,%d) = %s, expecting %s\n",
				  x, nu, value, expected);
		errcount++;
	    }
	}
	nu = 6;
	for (double[] data: tableP6) {
	    double x = data[0];
	    double value = ChiSquareDistr.P(x, nu);
	    double expected = data[1];
	    double ourlimit = (x == 0.1)? 1.e-8: limit;
	    ourlimit = (x == 1.5)? 1.e-8: ourlimit;
	    if (Math.abs((value - expected)/expected) > ourlimit) {
		System.out.format("P(%g,%d) = %s, expecting %s\n",
				  x, nu, value, expected);
		System.out.println(ourlimit);
		errcount++;
	    }
	}

	nu = 2;
	for (double[] data: tableQ2) {
	    double x = data[0];
	    double value = ChiSquareDistr.Q(x, nu);
	    double expected = data[1];
	    double ourlimit = (x == 3.3)? 1.e-7: limit;
	    if (Math.abs((value - expected)/expected) > ourlimit) {
		System.out.format("Q(%g,%d) = %s, expecting %s\n",
				  x, nu, value, expected);
		errcount++;
	    }
	}

	nu = 5;
	for (double[] data: tableQ5) {
	    double x = data[0];
	    double value = ChiSquareDistr.Q(x, nu);
	    double expected = data[1];
	    double ourlimit = limit;
	    if (x == 6.7) ourlimit = 1.e-8;
	    else if (x == 11.9) ourlimit = 1.e-8;
	    else if (x == 12.3) ourlimit = 1.e-8;
	    else if (x == 12.7) ourlimit = 1.e-8;
	    else if (x == 17.9) ourlimit = 1.e-8;
	    if (Math.abs((value - expected)/expected) > ourlimit) {
		System.out.format("Q(%g,%d) = %s, expecting %s\n",
				  x, nu, value, expected);
		errcount++;
	    }
	}

	nu = 6;
	for (double[] data: tableQ6) {
	    double x = data[0];
	    double value = ChiSquareDistr.Q(x, nu);
	    double expected = data[1];
	    double ourlimit = limit;
	    if (x == 5.7) ourlimit = 1.e-8;
	    else if (x == 6.9) ourlimit = 1.e-8;
	    else if (x == 7.7) ourlimit = 1.e-8;
	    else if (x == 13.7) ourlimit = 1.e-8;
	    else if (x == 14.5) ourlimit = 1.e-8;
	    else if (x == 20.1) ourlimit = 1.e-8;
	    if (Math.abs((value - expected)/expected) > ourlimit) {
		System.out.format("Q(%g,%d) = %s, expecting %s\n",
				  x, nu, value, expected);
		errcount++;
	    }
	}

	for (nu = 1; nu < 20; nu++) {
	    for (int i = 0; i < 300; i++) {
		double x = i/10.0;
		double p = ChiSquareDistr.P(x, nu);
		double q = ChiSquareDistr.Q(x, nu);
		if (Math.abs((p+q) - 1.0) > 1.e-10) {
		    System.out.format("P(%g, %d) + Q(%g, %d) = %s\n",
				      x, nu, x, nu, (p+q));
		    errcount++;
		}
	    }
	}

	ChiSquareDistr chisqd = new ChiSquareDistr(6);
	for (int i = 0; i < 300; i++) {
	    double x = i/10.0;
	    if (chisqd.pd(x) != ChiSquareDistr.pd(x, 6)) {
		System.out.format("pd(x) != pd(x, 6)\n", x);
		errcount++;
	    }
	    if (chisqd.P(x) != ChiSquareDistr.P(x, 6)) {
		System.out.format("P(x) != P(x, 6)\n", x);
		errcount++;
	    }
	    if (chisqd.Q(x) != ChiSquareDistr.Q(x, 6)) {
		System.out.format("Q(x) != Q(x, 6)\n", x);
		errcount++;
	    }
	}

	System.out.println("... now check ChiSquareStat");

	double test1[][] = {{2.0, 1.0},
			    {3.0, 2.0},
			    {4.0, 1.0},
			    {1.0, 1.0}
	};
	double test1x[] = new double[test1.length];
	double test1e[] = new double[test1.length];

	double test1xh[] = new double[test1.length/2];
	double test1eh[] = new double[test1.length/2];
	ChiSquareStat stat1 = new ChiSquareStat();
	double sum = 0.0;
	int i = 0;
	for (double[] values: test1) {
	    stat1.add(values[0], values[1]);
	    sum += (values[0] - values[1])*(values[0] - values[1])/values[1];
	    if (i < test1.length/2) {
		test1xh[i] = values[0];
		test1eh[i] = values[1];
	    }
	    test1x[i] = values[0];
	    test1e[i++] = values[1];
	}
	if (Math.abs(sum - stat1.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat1.getValue() = %s\n",
				  sum, stat1.getValue());
		errcount++;
	}

	if (stat1.getDegreesOfFreedom() != 4) {
	    System.out.println("stat1 degrees of freedom != 4");
	    errcount++;
	}
	stat1.setConstraints(1);
	if (stat1.getDegreesOfFreedom() != 3) {
	    System.out.println("stat1 degrees of freedom != 4");
	    errcount++;
	}

	ChiSquareStat stat1h = new ChiSquareStat(test1xh, test1eh);
	int cnt = test1xh.length;
	i = 0;
	for (double[] values: test1) {
	    if (!(i < test1xh.length)) {
		stat1h.add(values[0], values[1]);
		cnt++;
	    }
	    i++;
	}
	if (Math.abs(sum - stat1h.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat1h.getValue() = %s\n",
				  sum, stat1h.getValue());
		errcount++;
	}

	if (stat1h.getDegreesOfFreedom() != cnt) {
	    System.out.println("stat1h degrees of freedom != " + cnt);
	    errcount++;
	}

	ChiSquareStat stat2 = new ChiSquareStat(test1x, test1e);
	if (Math.abs(sum - stat2.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat2.getValue() = %s\n",
				  sum, stat1.getValue());
		errcount++;
	}

	if (stat2.getDegreesOfFreedom() != 4) {
	    System.out.println("stat2 degrees of freedom != 4");
	    errcount++;
	}

	int test3[][] = {
	    {36, 14},
	    {30, 25},
	};

	ChiSquareStat stat3 = new ChiSquareStat(test3);
	if ((stat3.getValue() - 3.418) > 1.e-3) {
	    System.out.format("stat3.getValue() = %g, expecting 3.418\n",
			       stat3.getValue());
	    errcount++;
	}
	if (stat3.getDegreesOfFreedom() != 1) {
	    System.out.println("stat3 degrees of freedom != 1");
	    errcount++;
	}

	int test4[][] = {{49, 50, 69},
			 {24, 36, 38},
			 {19, 22, 28}};

	ChiSquareStat stat4 = new ChiSquareStat(test4);
	if ((stat4.getValue() - 1.51) > 1.e-2) {
	    System.out.format("stat3.getValue() = %g, expecting 1.51\n",
			       stat3.getValue());
	    errcount++;
	}
	if (stat4.getDegreesOfFreedom() != 4) {
	    System.out.println("stat3 degrees of freedom != 4");
	    errcount++;
	}

	int test5[][] = {{200, 150, 50},
			 {250, 300, 50}};
	ChiSquareStat stat5 = new ChiSquareStat(test5);
	if ((stat5.getValue() - 16.2) > 1.e-1) {
	    System.out.format("stat5.getValue() = %g, expecting 16.2\n",
			       stat5.getValue());
	    errcount++;
	}
	if (stat5.getDegreesOfFreedom() != 2) {
	    System.out.println("stat5 degrees of freedom != 2");
	    errcount++;
	}

	double test6[][] = {{2.0, 1.0, 1.0},
			    {3.0, 2.0, 0.5},
			    {4.0, 1.0, 1.0},
			    {1.0, 1.0, 0.5}};
	double[] test6x = new double[test6.length];
	double[] test6e = new double[test6.length];
	double[] test6s = new double[test6.length];
	double[] test6xh = new double[test6.length/2];
	double[] test6eh = new double[test6.length/2];
	double[] test6sh = new double[test6.length/2];

	i = 0;
	sum = 0.0;
	ChiSquareStat stat6 = new ChiSquareStat();
	for (double[] values: test6) {
	    stat6.add(values[0], values[1], values[2]);
	    sum += (values[0] - values[1])*(values[0] - values[1])
		/(values[2]*values[2]);
	    if (i < test6.length/2) {
		test6xh[i] = values[0];
		test6eh[i] = values[1];
		test6sh[i] = values[2];
	    }
	    test6x[i] = values[0];
	    test6e[i] = values[1];
	    test6s[i++] = values[2];
	}
	if (Math.abs(sum - stat6.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat6.getValue() = %s\n",
				  sum, stat6.getValue());
		errcount++;
	}

	if (stat6.getDegreesOfFreedom() != 4) {
	    System.out.println("stat6 degrees of freedom != 4");
	    errcount++;
	}
	stat6.setConstraints(1);
	if (stat6.getDegreesOfFreedom() != 3) {
	    System.out.println("stat6 degrees of freedom != 4");
	    errcount++;
	}

	ChiSquareStat stat6h = new ChiSquareStat(test6xh, test6eh, test6sh);
	cnt = test6xh.length;
	i = 0;
	for (double[] values: test6) {
	    if (!(i < test6xh.length)) {
		stat6h.add(values[0], values[1], values[2]);
		cnt++;
	    }
	    i++;
	}
	if (Math.abs(sum - stat6h.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat6h.getValue() = %s\n",
				  sum, stat6h.getValue());
		errcount++;
	}

	if (stat6h.getDegreesOfFreedom() != cnt) {
	    System.out.println("stat6h degrees of freedom != " + cnt);
	    errcount++;
	}

	ChiSquareStat stat7 = new ChiSquareStat(test6x, test6e, test6s);
	if (Math.abs(sum - stat7.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat7.getValue() = %s\n",
				  sum, stat1.getValue());
		errcount++;
	}

	if (stat7.getDegreesOfFreedom() != 4) {
	    System.out.println("stat7 degrees of freedom != 4");
	    errcount++;
	}


	sum = 0.0;
	ChiSquareStat stat8 = new ChiSquareStat(test1x,test1e, 2.0);

	for (double[] values: test1) {
	    sum += (values[0] - values[1])*(values[0] - values[1])/4.0;
	}
	if (Math.abs(sum - stat8.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat8.getValue() = %s\n",
				  sum, stat8.getValue());
		errcount++;
	}

	if (stat8.getDegreesOfFreedom() != 4) {
	    System.out.println("stat8 degrees of freedom != 4");
	    errcount++;
	}

	sum = 0.0;
	ChiSquareStat stat9 = new ChiSquareStat(test1x, 0.5, 2.0);

	for (double[] values: test1) {
	    sum += (values[0] - 0.5)*(values[0] - 0.5)/4.0;
	}
	if (Math.abs(sum - stat9.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat9.getValue() = %s\n",
				  sum, stat9.getValue());
		errcount++;
	}

	if (stat9.getDegreesOfFreedom() != 4) {
	    System.out.println("stat9 degrees of freedom != 4");
	    errcount++;
	}


	sum = 0.0;
	ChiSquareStat stat10 = new ChiSquareStat(test1x, 0.5);

	for (double[] values: test1) {
	    sum += (values[0] - 0.5)*(values[0] - 0.5)/(0.5);
	}
	if (Math.abs(sum - stat10.getValue()) > 1.e-10) {
		System.out.format("sum = %s, stat10.getValue() = %s\n",
				  sum, stat10.getValue());
		errcount++;
	}

	if (stat10.getDegreesOfFreedom() != 4) {
	    System.out.println("stat10 degrees of freedom != 4");
	    errcount++;
	}

	double xnc = 1.5;
	long nunc = 4;
	double lambda = 2.1;

	System.out.println("non-central case");

	// Used http://keisan.casio.com/exec/system/1180573182
	// to get the comparison values
	double testval = 0.0898166971976661790467;

	if (Math.abs(ChiSquareDistr.pd(xnc, nunc, lambda)
		     - testval) > 1.e-10) {
	    System.out.format("expected %s, got %s\n",
			       testval, ChiSquareDistr.pd(xnc, nunc, lambda));
	    errcount++;
	}
	testval = 0.077028727277614826752;
	if (Math.abs(ChiSquareDistr.P(xnc, nunc, lambda)
		     - testval ) > 1.e-10) {
	    System.out.format("expected %s, got %s\n",
			       testval, ChiSquareDistr.P(xnc, nunc, lambda));
	    errcount++;
	}
	testval = 0.922971272722385173248;
	if (Math.abs(ChiSquareDistr.Q(xnc, nunc, lambda)
		     - testval) > 1.e-10) {
	    System.out.format("expected %s, got %s\n",
			       testval, ChiSquareDistr.Q(xnc, nunc, lambda));
	    errcount++;
	}

	if (errcount > 0)  {
	    throw new Exception("chi-square test failed");
	}


	System.exit(0);
    }
}
