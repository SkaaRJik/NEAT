package ru.filippov.prediction;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.neat4j.core.AIConfig;
import org.neat4j.core.InitialisationFailedException;
import org.neat4j.neat.core.DefaultConfig;
import org.neat4j.neat.core.NEATConfig;
import org.neat4j.neat.core.NEATLoader;
import org.neat4j.neat.data.core.DataKeeper;
import org.neat4j.neat.ga.core.Chromosome;
import sun.rmi.runtime.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class WindowPredictionTest {

    Logger logger = Logger.getLogger(WindowPredictionTest.class);

    private WindowPrediction windowPrediction;
    private DataKeeper dataKeeper;
    final static  int WINDOW_SIZE = 3;
    AIConfig config;

    public WindowPredictionTest(){
        List<List<Double>> data = new ArrayList<List<Double>>(24);
        data.add(Arrays.asList(0.5781021897810219, 0.4012345679012346 ,0.6, 0.46598924002457615, 0.5354963075440319, 0.40028353988890714));
        data.add(Arrays.asList(0.5401459854014599, 0.4, 0.42135054256305526, 0.4605826329564896, 0.49535229255694135, 0.40049028109484797));
        data.add(Arrays.asList(0.41167883211678835, 0.44938271604938274, 0.4115564402492105, 0.500500012230515, 0.5182105152405437, 0.40051669390155875));
        data.add(Arrays.asList(0.4233576642335767, 0.5172839506172839, 0.41703623511767546, 0.5279288845914216, 0.5078796149335374, 0.4000975894866422));
        data.add(Arrays.asList(0.44963503649635034, 0.508641975308642, 0.4270024193411317, 0.6, 0.6, 0.40023654842957834));
        data.add(Arrays.asList(0.46715328467153283, 0.591358024691358, 0.42567593611941695, 0.5348055995614281, 0.5050418182690614, 0.4));
        data.add(Arrays.asList(0.4642335766423358, 0.6, 0.41253493295492427, 0.523080564560572, 0.46475265819935724, 0.4107910635609873));
        data.add(Arrays.asList(0.43503649635036495, 0.5419753086419753, 0.4368586700838704, 0.48041434107021325, 0.4240387473355779, 0.41618574673724934));
        data.add(Arrays.asList(0.4467153284671533, 0.5641975308641975, 0.42676627969016756, 0.47987648618743195, 0.441999650314207, 0.42203644861250555));
        data.add(Arrays.asList(0.4905109489051095, 0.5555555555555556, 0.4177649081826517, 0.47369000392815364, 0.45575097665840764, 0.4313281345716591));
        data.add(Arrays.asList(0.4583941605839416, 0.5185185185185185, 0.4259140034032425, 0.46484820492012036, 0.46401124401351623, 0.4400362990460097));
        data.add(Arrays.asList(0.48029197080291974, 0.537037037037037, 0.4158723837428253, 0.42685331871427074, 0.4057143603620558, 0.45590010655286883));
        data.add(Arrays.asList(0.4029197080291971, 0.5172839506172839, 0.408790643887543, 0.418731537318179, 0.4, 0.46887102223396127));
        data.add(Arrays.asList(0.4, 0.4765432098765432, 0.4181430471885953, 0.4167453016836383, 0.41182670238318186, 0.4811821481249294));
        data.add(Arrays.asList(0.47007299270072994, 0.49506172839506174, 0.4327729980892768, 0.4056571167927849, 0.4028298890407101, 0.4999064944212227));
        data.add(Arrays.asList(0.48759124087591244, 0.4876543209876543, 0.4130522184119569, 0.4062673475466307, 0.42928155217276176, 0.5172911067867654));
        data.add(Arrays.asList(0.4890510948905109, 0.4765432098765432, 0.43114169075731973, 0.40988369499685606, 0.45840942426819137, 0.5361634284459563));
        data.add(Arrays.asList(0.48759124087591244, 0.5024691358024691, 0.4001243327730327, 0.41673192006135407, 0.45001363060661853, 0.5319491537559489));
        data.add(Arrays.asList(0.5795620437956205, 0.45679012345679015, 0.4003846294167113, 0.41091954767238914, 0.4418369527017193, 0.5450944576066497));
        data.add(Arrays.asList(0.6, 0.4419753086419753, 0.4001278598133237, 0.4, 0.42303344705591683, 0.5669159971974845));
        data.add(Arrays.asList(0.5737226277372263, 0.42962962962962964, 0.4, 0.41140344440079835, 0.45863710294912624, 0.5790258978100177));
        data.add(Arrays.asList(0.5576642335766424, 0.42716049382716054, 0.40000471605256227, 0.428222416951206, 0.511817033686574, 0.5920469872163077));
        data.add(Arrays.asList(0.4978102189781022, 0.4320987654320988, 0.4002285300631235, 0.42034783584634444, 0.48060171559440445, 0.6));
        

        List<String > headers = Arrays.asList("Инвестиции в ОК,в % к ВРП   ( Вход )" , "Уровень безработицы, %   ( Вход )" , "Инвестиции в ОК на ОПС, % к ВРП   ( Вход )" , "Эко-инт. по воздуху, куб.м/1000 руб.    ( Вход )" , "Экологоемкость, воздух кг/чел  ( Вход )" , "ВРП на д. н., в тек. ценах, руб/чел ( Выход )");
        String legengHeader = "Год";

        List<Double> legend = Arrays.asList(1992.0, 1993.0, 1994.0, 1995.0, 1996.0, 1997.0, 1998.0, 1999.0, 2000.0, 2001.0, 2002.0, 2003.0, 2004.0, 2005.0, 2006.0, 2007.0, 2008.0, 2009.0, 2010.0, 2011.0, 2012.0, 2013.0, 2014.0);
        this.dataKeeper = new DataKeeper(data, null);
        this.dataKeeper.setHeaders(headers);
        this.dataKeeper.setInputs(5);
        this.dataKeeper.setOutputs(1);
        this.dataKeeper.setLegendHeader(legengHeader);
        this.dataKeeper.setLegend(legend);

        try {
            this.windowPrediction = new WindowPrediction();
            this.windowPrediction.initialise(this.dataKeeper, WINDOW_SIZE, 3, DefaultConfig.getDefaultConfig());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        }

        config = new NEATLoader().loadConfig("F:\\JavaProjects\\NEATJavaFX\\src\\test\\java\\ru\\filippov\\testResources\\Start.neat");

    }

    @Test
    void configIsLoaded(){
        Assertions.assertNotNull(config);
    }

    @Test
    void prepareDataOneColumnTest(){
        DataKeeper dataKeeper1 = windowPrediction.prepareDataForWindow(0, dataKeeper);
        Assertions.assertEquals(WINDOW_SIZE+1, dataKeeper1.getData().get(0).size());
    }




    @Test
    void startTest(){

        Thread thread = new Thread(this.windowPrediction);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Test
    void retainTest(){

        try {
            windowPrediction.retrain(1, DefaultConfig.getDefaultConfig()).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Chromosome> bestEverChromosomes = windowPrediction.trainer[1].getBestEverChromosomes();
        double fitness1 = bestEverChromosomes.get(bestEverChromosomes.size() - 1).fitness();
        try {
            windowPrediction.retrain(1, DefaultConfig.getDefaultConfig()).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bestEverChromosomes = windowPrediction.trainer[1].getBestEverChromosomes();
        double fitness2 = bestEverChromosomes.get(bestEverChromosomes.size() - 1).fitness();

        Assertions.assertEquals(fitness1, fitness2);

        AIConfig newConfig = new NEATConfig((NEATConfig) DefaultConfig.getDefaultConfig());
        newConfig.updateConfig("GENERATOR.SEED", String.valueOf(System.currentTimeMillis()));
        try {
            windowPrediction.retrain(1, newConfig).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bestEverChromosomes = windowPrediction.trainer[1].getBestEverChromosomes();
        double fitness3 = bestEverChromosomes.get(bestEverChromosomes.size() - 1).fitness();
        Assertions.assertNotEquals(fitness1, fitness3);
        Assertions.assertNotEquals(fitness2, fitness3);
    }


    @Test
    void tryToPredictWithNoTraining(){
        InitialisationFailedException thrown = assertThrows(InitialisationFailedException.class, () -> windowPrediction.predict(config));
        Assertions.assertNotNull(thrown);
    }

    @Test
    void predictTest(){
        Thread thread = new Thread(this.windowPrediction);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            this.windowPrediction.predict(config);
        } catch (InitialisationFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    void reverseTest(){
        String hello = "hello";
        System.out.println(new StringBuilder(hello).reverse().toString());



    }

}