package users;

import Utils.Colors;
import Utils.enums.EnumMonths;
import algorithms.matrioshka.Day;
import algorithms.matrioshka.Month;
import algorithms.matrioshka.Year;
import interpreters.InterpreterWhatsapp;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import users.Person;
import users.data.Date;

public class PersonManager{
    //Variable estatica con la ruta del chat a analizar
    private static String filePath = "../../Chats/ChatBase(total).txt";
    private InterpreterWhatsapp InterWhatsapp;
    private List<Person> persons;
    private boolean analysisCompleted;
    private int totalDays;
    private Date mostActiveDate;
    private String firstTalkedDate;
    public PersonManager(){
        analysisCompleted = false;
        //Crear más adelante una clases que gestione todo esto
        //Inicializamos la clase interprete que analizará y devolverá el chat de whatsapp
        InterWhatsapp = new InterpreterWhatsapp(Utils.Auxiliary.chatPath);
        //Inicializamos la lista de personas con todos sus mensajes así como la información pertinente del chat
        persons = InterWhatsapp.chatInterpret();
    }
    //Guardaremos el número total de personas acutales para reutilizarlo sin llamar al metodo
    private int totalPersons;
    //Creamos un array de cada "Persona" almacenando su extructura matrishka con todos los datos
    private LinkedHashMap<Integer, Year>[] personsMatrishka;
    //Totales globales
    private int messagesGlobal;
    private int wordGlobal;
    private int charsGlobal;
    private int daysGlobal;
    //Media de mensajes/words/chars mensuales de todas las personas
    private double[][] grupalAverageMonthsMessages;
    private double[][] grupalAverageMonthsWords;
    private double[][] grupalAverageMonthsChars;
    
    float dayAverageMessage;
    float dayAverageWord;
    //Count Messages per hour
    private double[] dataHours;
    //Guarda el día que comienza y finaliza la conversación
    java.util.Date firstDateComvo;
    java.util.Date lastDateComvo;
    //Arranque inicial del algoritmo
    public boolean startAlgorythm(){
        //Pruebas del algoritmo
        for (int i = 0, t = persons.size(); i < t; i++){
            //Inicia el algoritmo de conteo y medias (Cada clase Person, le enviará sus mensajes uno a uno para que los analice y guarde los datos deseados)
            persons.get(i).startAlgorythm();
            //Crea un log sencillo con todos los años, así como los meses, días y horas que tiene cada uno
            //persons.get(i).createLogOfTheMatrioshkaStructure();
        }
        //Inizialización de variables auxiliares
        auxiliaryInitialization();
        //Calculos post almacenaje de mensajes en la matrioshka
        getTotalNumber();
        totalDaysChat();
        getPercentages();
        //Saca la media grupal de lo que se habla mensualmente(messages/words/chars) en la conversación
        getGrupAverageMonthsMessages();
        getGrupAverageMonthsWords();
        getGrupAverageMonthsChars();
        //comentar algo
        getCountHourAverageMessagesPerson();
        //Exportar/crear los json necesarios para las gráficas una vez ya tenemos todos los números
        return exportJSData();
    }
    //Inicialización de variables auxiliares que necesitamos para obtener más numero y crear los fichertos json
    public void auxiliaryInitialization(){
        totalPersons = persons.size();
        personsMatrishka = new LinkedHashMap[totalPersons];
        //Obtenermos la matrioshka de todas las personas
        for (int i = 0, t = totalPersons; i < t; i++)
            personsMatrishka[i] = persons.get(i).getMatrioshka();
        messagesGlobal = 0;
        wordGlobal = 0;
        charsGlobal = 0;
        daysGlobal = 0;
        totalDays = 0;
        dayAverageMessage = 0;
        dayAverageWord = 0;
        mostActiveDate = new Date(0,0,0);
        //Almacena los días que comenzó y terminó la conversación
        int firstDayComvo = 0;
        int lastDayComvo = 0;
        //System.out.println("IntSDate: "+persons.get(0).getIntegerFromFirstDate());
        //System.out.println("IntFDate: "+persons.get(0).getMessageFromLastDate());
        for(int i = 1; i < totalPersons;i++){
            System.out.println("IntSDate: "+persons.get(i).getMessageFromFirstDate());
            System.out.println("IntFDate: "+persons.get(i).getMessageFromLastDate());
            if(persons.get(firstDayComvo).getMessageFromFirstDate().compareTo(persons.get(i).getMessageFromFirstDate()) > 0)
                firstDayComvo = i;
            if(persons.get(lastDayComvo).getMessageFromLastDate().compareTo(persons.get(i).getMessageFromLastDate()) < 0)
                lastDayComvo = i;
        }
        System.out.println("El menor es el: "+firstDayComvo+" / "+persons.get(firstDayComvo).getMessageFromFirstDate().toString());
        System.out.println("El mayor es el: "+lastDayComvo+" / "+persons.get(lastDayComvo).getMessageFromLastDate().toString());
        firstDateComvo = persons.get(firstDayComvo).getMessageFromFirstDate();
        firstTalkedDate = persons.get(firstDayComvo).getMessageFromFirstDateOurn();
        lastDateComvo = persons.get(lastDayComvo).getMessageFromLastDate();
        
    }
     //Con este metodo obtenemos el numero total de mensajes,words y chars usando variables globales
    //El valor de cada variable global lo obtenemos en la clase "AlgorithmCMT.java"
    public void getTotalNumber(){
        //Suma de los tops globales de cada persona para agruparlo
        for(int i = 0; i < totalPersons; i++){
            messagesGlobal += persons.get(i).getMessagesGlobal();
            wordGlobal += persons.get(i).getWordsGlobal();
            charsGlobal += persons.get(i).getCharsGlobal();
        }
        System.out.println("Numero total de mensajes --> "+ Colors.ANSI_YELLOW + messagesGlobal + Colors.ANSI_RESET);
        System.out.println("Numero total de palabras --> "+ Colors.ANSI_YELLOW + wordGlobal + Colors.ANSI_RESET);
        System.out.println("Numero total de chars --> "+ Colors.ANSI_YELLOW + charsGlobal + Colors.ANSI_RESET);
    }
    public void getPercentages(){
        for(int i = 0; i < totalPersons; i++){
            System.out.println(persons.get(i).getName() + " --> "+ Colors.ANSI_YELLOW + persons.get(i).getPercentageSpoke()+ Colors.ANSI_RESET);
        }
    }
    //Calcula y anota la media mensual de messages que se habla de manera grupal en una conversación
    public void getGrupAverageMonthsMessages(){
        //Guardo la cantidad de años que hay en este chat, dado que se usa mucho este valor
        int totalYears = persons.get(0).getAverageMonthMessages().length;
        //Al igual que en su versión individual, se usa una variable local para almacenar el conteo de mensajes mensuales de cada persona
        double totalAverage = 0.0f;
        //Inicializamos el array 2D donde se almacenará la media grupal
        grupalAverageMonthsMessages = new double[totalYears][12];
        //Conjunto de bucles usados para extraer la media de cada media mensual individual, por cada año de las diferentes personas que han hablado en el chat.
        for (Person p : persons){
            for (int year = 0, tY = persons.get(year).getAverageMonthMessages().length; year < tY; year++){ 
                for (int month = 0, tM = p.getAverageMonthMessages()[year].length; month < tM; month++){
                    totalAverage += p.getOneAverageMonthMessages(year, month);
                   
                    //Se calcula la media de este mes y se almacena
                    grupalAverageMonthsMessages[year][month] = totalAverage / totalPersons;
                    totalAverage = 0;
                }
            }
        }
    }
    //Calcula y anota la media mensual de words que se habla de manera grupal en una conversación
    public void getGrupAverageMonthsWords(){
        //Guardo la cantidad de años que hay en este chat, dado que se usa mucho este valor
        int totalYears = persons.get(0).getAverageMonthWords().length;
        //Al igual que en su versión individual, se usa una variable local para almacenar el conteo de words mensuales de cada persona
        double totalAverage = 0.0f;
        //Inicializamos el array 2D donde se almacenará la media grupal
        grupalAverageMonthsWords = new double[totalYears][12];
        //Conjunto de bucles usados para extraer la media de cada media mensual individual, por cada año de las diferentes personas que han hablado en el chat.
        for (Person p : persons){
            for (int year = 0, tY = persons.get(year).getAverageMonthWords().length; year < tY; year++){ 
                for (int month = 0, tM = p.getAverageMonthWords()[year].length; month < tM; month++){
                    totalAverage += p.getOneAverageMonthWords(year, month);
                   
                    //Se calcula la media de este mes y se almacena
                    grupalAverageMonthsWords[year][month] = totalAverage / totalPersons;
                    totalAverage = 0;
                }
            }
        }
    }
    //Calcula y anota la media mensual de chars que se habla de manera grupal en una conversación
    public void getGrupAverageMonthsChars(){
        //Guardo la cantidad de años que hay en este chat, dado que se usa mucho este valor
        int totalYears = persons.get(0).getAverageMonthChars().length;
        //Al igual que en su versión individual, se usa una variable local para almacenar el conteo de chars mensuales de cada persona
        double totalAverage = 0.0f;
        //Inicializamos el array 2D donde se almacenará la media grupal
        grupalAverageMonthsChars = new double[totalYears][12];
        //Conjunto de bucles usados para extraer la media de cada media mensual individual, por cada año de las diferentes personas que han hablado en el chat.
        for (Person p : persons){
            for (int year = 0, tY = persons.get(year).getAverageMonthChars().length; year < tY; year++){ 
                for (int month = 0, tM = p.getAverageMonthChars()[year].length; month < tM; month++){
                    totalAverage += p.getOneAverageMonthChars(year, month);
                   
                    //Se calcula la media de este mes y se almacena
                    grupalAverageMonthsChars[year][month] = totalAverage / totalPersons;
                    totalAverage = 0;
                }
            }
        }
    }
    
    //Count messages per hour
    public void getCountHourAverageMessagesPerson(){
        int personsSize = persons.size();
        dataHours = new double[24];
        double[][] data = new double[personsSize][24];
        for (int i = 0, t = personsSize; i < t; i++) {
            data[i] = persons.get(i).getCountHourPerson();
         }
     
        for (int i = 0; i < 24; i++) {
            for (int p = 0; p < personsSize; p++){ 
                dataHours[i] += data[p][i];
            }
        }
    }
    
    //Creación y expotación de ficheros JSon
    public boolean exportJSData(){       
        try(FileOutputStream oFileMessages = new FileOutputStream(Utils.Auxiliary.jSDataPatch, false)){
            //Cargar fichero de texto
            StringBuilder jSData = new StringBuilder();
            //Guardar los nombres de los usuarios del chat
            for (int i = 0, t = persons.size(); i < t; i++)
                jSData.append(String.format("var name%d = \"%s\"%n", i, persons.get(i).getName()));
            //JS de conteos básicos (Mensajes, palabras, caracteres)
            jSData.append(jSonCount());
            //JS con los datos de la tabla
            jSData.append(jSTable());
            //JS media individual y media general
            jSData.append(jSonAverage());
            //JS con todos los mensajes, para sacar las palabras más usadas
            jSData.append(jSonWordsMostUsed());
            //JS con la media para la grafica de percentage de la convo
            jSData.append(jSonPercentage());
            //JS con la frecuencia hablada, mostrada en horas
            jSData.append(jSHourAverageMessagesPerson());
            //Creamos el javaScript Con toda la información
            oFileMessages.write(jSData.toString().getBytes());
        }
        catch (Exception e){
            System.out.println("Error: " + e);
            return false;
        }
        return true;
    }
    //Datos de la tabla
    public String jSTable(){
        return String.format("var totalDays = %d%nvar totalMessagesCount = %d%nvar totalWordsCount = %d%nvar totalChartsCount = %d%nvar firstDate = '%s'%nvar mostActiveDate = '%s'%n", 
            totalDays, messagesGlobal, wordGlobal, charsGlobal, firstTalkedDate, mostActiveDate.toString());
    }
    //JSon (chat): Conteos 
    public String jSonCount(){
        //Dejo guardada la coletilla de cada indicador de fecha
        String beginingDate = "{\"date\": \"";
        StringBuilder jSonMessages = new StringBuilder();
        StringBuilder jSonWords = new StringBuilder();
        StringBuilder jSonChars = new StringBuilder();
        //Usamos un contador para los meses, ya que es necesaria su representación numérica en el json
        int counterMonths;
        //Calculo de día más activo
        /*float dayAverageMessage = 0.0f;
        float dayAverageWord = 0.0f;*/
        int totalLocalMessage = 0;
        int totalGlobalMessage = 0;
        
        //Abrimos brackets para empezar la extructura del json
        jSonMessages.append("var dataMessagesCount = [");
        jSonWords.append("var dataWordsCount = [");
        jSonChars.append("var dataChartsCount = [");
        //Estos fors son pareceidos a los de la matrioshka, con la diferentea de que estos se usan para recorrers todos los días de los años del calensario que han hablado estas personas en el chat.
        //Gracias a estos fors, podemos recorrer todas las fechas y anotarlas en el json
        for(HashMap.Entry<Integer, Year> y : personsMatrishka[0].entrySet()){
            counterMonths = 1;
            for(HashMap.Entry<String, Month> m : personsMatrishka[0].get(y.getKey()).getAllMonths().entrySet()){
                for(Day d : personsMatrishka[0].get(y.getKey()).getOneMonth(m.getKey()).getDays()){
                    //Delimita la inforamción exportada a partir del día que comienza y termina la conversación
                    float activeLocalNumber = 0;
                    if(insideOfDateRangeCombo(new java.util.Date(y.getKey(), counterMonths, d.getName()))){
                        //Escribimos la fecha en el json
                        jSonMessages.append(beginingDate + y.getKey() + "-" + String.format("%02d", counterMonths) + "-" + d.getNameString() + "\",");
                        jSonWords.append(beginingDate + y.getKey() + "-" + String.format("%02d", counterMonths) + "-" + d.getNameString() + "\",");
                        jSonChars.append(beginingDate + y.getKey() + "-" + String.format("%02d", counterMonths) + "-" + d.getNameString() + "\",");
                        //Limite de personas preparado de antemano
                        //Dentro de esta fecha, anotamos las personas correspondientes, y el número de mensajes que tiene (preparado para recibir un número indefinído de personas)
                        for (int i = 0; i < totalPersons; i++){
                            if(i == totalPersons-1){
                                jSonMessages.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount() + "");
                                jSonWords.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getWordCount() + "");
                                jSonChars.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getCharCount() + "");
                            }else{
                                jSonMessages.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount() + ",");
                                jSonWords.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getWordCount() + ",");
                                jSonChars.append("\"" + persons.get(i).getName() + "\": " + personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getCharCount() + ",");
                            }
                            dayAverageMessage = personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount();
                            dayAverageWord = personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getWordCount();
                            if(dayAverageMessage != 0 || dayAverageWord != 0){
                                activeLocalNumber += dayAverageWord / dayAverageMessage;
                                //System.out.println("X: "+ dayAverageWord + "Y: " + dayAverageMessage + " = " +activeLocalNumber);
                            }
                            totalLocalMessage += personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount();
                        }
                        //Contador de días hablados
                        if(activeLocalNumber > 0)
                            totalDays++;
                        //Se cierra esta fecha
                        jSonMessages.append("},\n");
                        jSonWords.append("},\n");
                        jSonChars.append("},\n");
                        //calculo del día más activo
                        /*if(activeLocalNumber > activeGlobalNumber){
                            activeGlobalNumber = activeLocalNumber;
                            System.out.println("ACTIVE");
                            mostActiveDate = new Date(y.getKey(), counterMonths, d.getName());
                        }*/
                        if(totalLocalMessage > totalGlobalMessage){
                            totalGlobalMessage = totalLocalMessage;
                            mostActiveDate = new Date(y.getKey(), counterMonths, d.getName());
                        }
                    }
                    totalLocalMessage = 0;
                    activeLocalNumber = 0;
                }
                //Tenemos en cuenta que el counter de meses no se pase de 12
                counterMonths++;
                if(m.getKey().equals(EnumMonths.DECEMBER))
                    counterMonths = 1;
                dayAverageMessage = 0;
                dayAverageWord = 0;
            }
        }
        //Elimina la última coma innecesaria
        jSonMessages.setLength(jSonMessages.length() - 2);
        jSonWords.setLength(jSonWords.length() - 2);
        jSonChars.setLength(jSonChars.length() - 2);
        //Cerramos la extructura del json
        jSonMessages.append("]\n");
        jSonWords.append("]\n");
        jSonChars.append("]\n");
        return jSonMessages.toString() + jSonWords.toString() + jSonChars.toString();
    }
    //JSon (chat): Medias
    //Media al dia de mensajes/words/chars de cada mes 
    public String jSonAverage(){
        //Dejo guardada la coletilla de cada conjunto de datos
        String beginingDate = "{\"category\": \"";
        int totalYears = persons.get(0).getTotalYears();
        //Creo los StringBuilder que serán el contenido de cada json más tarde
        StringBuilder jSonAverageM = new StringBuilder();
        StringBuilder jSonAverageW = new StringBuilder();
        StringBuilder jSonAverageC = new StringBuilder();
        //Abrimos brackets para empezar la extructura del json
        jSonAverageM.append("var dataMessagesAverage = [");
        jSonAverageW.append("var dataWordsAverage = [");
        jSonAverageC.append("var dataCharsAverage = [");
        //Estos fors son una versión sencialla de la matrioshka, adaptado a las medias
        //Gracias a estos fors, podemos recorrer todas las medias de cada personas. E incluso las generales
        for (int y = 0; y < totalYears; y++){          
            //for para recorrer los meses de cada media
            for (int m = 0; m < 12; m++){
                //Delimita la inforamción exportada a partir del día que comienza y termina la conversación
                if(insideOfDateRangeCombo(new java.util.Date((firstDateComvo.getYear()+y), (m+1), 1)) || 
                    insideOfDateRangeCombo(new java.util.Date((firstDateComvo.getYear()+y), (m+1), Utils.Auxiliary.getNumberDaysGivenYear((firstDateComvo.getYear()+y), (m+1))))){
                    //Guardamos el inicio de cada conjunto de datos con el nombre de su mes
                    jSonAverageM.append(String.format("%s%d %s\", ", beginingDate, persons.get(0).getYearNumber(y), EnumMonths.values()[m+1].name()));
                    jSonAverageW.append(String.format("%s%d %s\", ", beginingDate, persons.get(0).getYearNumber(y), EnumMonths.values()[m+1].name()));
                    jSonAverageC.append(String.format("%s%d %s\", ", beginingDate, persons.get(0).getYearNumber(y), EnumMonths.values()[m+1].name()));
                    //Usaremos estos fors para guardar las medias de cada una de las personas
                    for (int p = 0; p < totalPersons; p++){
                        jSonAverageM.append(String.format(Locale.US, "\"%s\": %f, ", persons.get(p).getName(), persons.get(p).getAverageMonthMessages()[y][m]));
                        jSonAverageW.append(String.format(Locale.US, "\"%s\": %f, ", persons.get(p).getName(), persons.get(p).getAverageMonthWords()[y][m]));
                        jSonAverageC.append(String.format(Locale.US, "\"%s\": %f, ", persons.get(p).getName(), persons.get(p).getAverageMonthChars()[y][m]));
                    }
                    //Guardamos las medias generales
                    jSonAverageM.append(String.format(Locale.US, "\"general\": %f},\n", grupalAverageMonthsMessages[y][m]));
                    jSonAverageW.append(String.format(Locale.US, "\"general\": %f},\n", grupalAverageMonthsWords[y][m]));
                    jSonAverageC.append(String.format(Locale.US, "\"general\": %f},\n", grupalAverageMonthsChars[y][m]));
                }
            }          
        }
        //Elimina la última coma innecesaria
        jSonAverageM.setLength(jSonAverageM.length() - 2);
        jSonAverageW.setLength(jSonAverageW.length() - 2);
        jSonAverageC.setLength(jSonAverageC.length() - 2);
        //Cerramos la extructura del json
        jSonAverageM.append("]\n");
        jSonAverageW.append("]\n");
        jSonAverageC.append("]\n");
        return (jSonAverageM.toString() + jSonAverageW.toString() + jSonAverageC.toString());
    }
    public String jSonPercentage(){

        StringBuilder jSonPercent = new StringBuilder();
        jSonPercent.append("var dataChatGlobalPercentage = [");

        for (int i = 0; i < totalPersons; i++){
            if(i == totalPersons-1){
                 jSonPercent.append(
                         "{ name: \"" + persons.get(i).getName() + "\", value: " + persons.get(i).getPercentageSpoke() + " }");
            }else{
                 jSonPercent.append(
                         "{ name: \"" + persons.get(i).getName() + "\", value: " + persons.get(i).getPercentageSpoke() + " },");
            }    
        }
        //Elimina la última coma innecesaria
        //jSonPercent.setLength(jSonPercent.length() - 2);
        //Cerramos la extructura del json
        jSonPercent.append("]\n");
        return  jSonPercent.toString();
    }
    //Metodo que saca el numero total de dias de una conversación
    public void totalDaysChat(){
        for(HashMap.Entry<Integer, Year> y : personsMatrishka[0].entrySet()){
            for(HashMap.Entry<String, Month> m : personsMatrishka[0].get(y.getKey()).getAllMonths().entrySet()){
                for(Day d : personsMatrishka[0].get(y.getKey()).getOneMonth(m.getKey()).getDays()){
                    boolean daysTalked = false;
                    for (int i = 0; i < totalPersons && !daysTalked; i++){
                        if(personsMatrishka[i].get(y.getKey()).getOneMonth(m.getKey()).getOneDay(d.getArrayName()).getMessageCount()>0 ){
                            daysGlobal++;
                            daysTalked = true;
                        }
                    }
                }
            }
        }
        System.out.println("Dias totales hablados --> "+ Colors.ANSI_YELLOW +daysGlobal+ Colors.ANSI_RESET); 
    }
   //Con este metodo, extraemos todos los mensajes(textos) de cada persona, y los almaceno en un para asu posterior exportación en json
    public String jSonWordsMostUsed(){
        StringBuilder jSonAllMessages = new StringBuilder();
        jSonAllMessages.append("[{");
        for (Person p : persons){
            for (int i = 0, t = p.getTotalMessages(); i < t; i++){
                //Siempre se eliminan los saltos de línea(replaceAll) para que no sucedan errores cuando se lea el archivo más tarde.
                jSonAllMessages.append(p.getMessageObj(i).getText().replaceAll("\\r|\\n", " ") + ", ");
            }
        }
        jSonAllMessages.append("}]\n");
        return countWordsFromHugeString(jSonAllMessages.toString());
        //Más tarde se usará este TopWordsMostUsed.json para calcular las palabras más usadas en la conversación
        /*try(FileOutputStream oFileChars = new FileOutputStream("web/javaScript/"+"TopWordsMostUsed.js", false)){
            oFileChars.write(countWordsFromHugeString(jSonAllMessages.toString()).getBytes());
        } 
        catch (Exception e){
            System.out.println("Error: " + e);
        }*/
    }
    
    //Contar el numero de veces que se repite cada palabra de la string con todos los messages concatenados del chat de cada persona
    public String  countWordsFromHugeString(String texto){
        Map<String, Integer> wordsWithCount = Utils.Auxiliary.countAndPrintRepeatedWordOccurences(texto);
        StringBuilder output = new StringBuilder();
        output.append("var dataChartTops = [");
        int limit = (wordsWithCount.size() > 190) ? 190 : wordsWithCount.size();
        int limitCounter = 1;
        //System.out.println("límite: " + limit);
        // Step 10: Again print after sorting
        for(Map.Entry<String, Integer> entry : wordsWithCount.entrySet()) {
            
            if((limit-1) < (limitCounter++)){
                output.append(String.format("{ word: \"%s\", weight: \"%s\"}]%n", entry.getKey(), entry.getValue()));
                break;
            }
            else
                output.append(String.format("{ word: \"%s\", weight: \"%s\"},%n", entry.getKey(), entry.getValue()));
        }      
        return output.toString();
    }

    //Con este metodos sacamos la media de la frecuencia en la que se suele hablar en la conversación (Mostrada den formato 24h)
    public String jSHourAverageMessagesPerson(){
        StringBuilder jSHourAverage = new StringBuilder();
        jSHourAverage.append("var HourAverage = [");
        for (int i = 0; i < 24; i++){
            jSHourAverage.append(String.format(Locale.US,"{hour: '%02d', data: '%.2f'},\n", i, dataHours[i]));
        }
        //Elimina la última coma innecesaria
        jSHourAverage.setLength(jSHourAverage.length() - 2);
        jSHourAverage.append("]");
        return jSHourAverage.toString();
    }
    public void importJSonFileMessagesData(String jsonData){
        String jsonFile = "src/web/index.js";
        StringBuilder fullFile = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(
            new FileInputStream(jsonFile), StandardCharsets.UTF_8));){
            String data = "";
            fullFile.append("var data = " + jsonData);
            while((data = br.readLine()) != null){
                fullFile.append(data + "\n");
            }
            
            FileOutputStream oFileWords = new FileOutputStream("src/web/newindex.js", false);
            oFileWords.write(fullFile.toString().getBytes());
        }
        catch (Exception e){
            System.out.println("Algo fue mal: " + e);
        }
    }
    //Comprobar meses
    public boolean insideOfDateRangeCombo(java.util.Date date){
        //System.out.println(Colors.ANSI_YELLOW+"Date: " + date.()+Colors.ANSI_GREEN+"\nFirst: " +firstDateComvo.getIntegerFromDate()+Colors.ANSI_CYAN+"\nLast: " +  lastDateComvo.getIntegerFromDate()+Colors.ANSI_RESET);
        if((date.compareTo(firstDateComvo) >= 0) && (date.compareTo(lastDateComvo)) <= 0)
            return true;
        else
            return false;
    }

}
