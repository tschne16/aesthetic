package com.aesthetic.net;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.spi.LoggerFactory;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;

import static java.lang.Math.toIntExact;

public class ConvolutionalNeuralNetwork {
	private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;

    protected static long seed = 42;
    protected static Random rng = new Random(seed);
    protected static double splitTrainTest = 0.8;
    static int batchSize = 128;
    static int epochscounter = 50;
    
    //private static final long seed = 12345;

    private static final Random randNumGen = new Random(seed);

    private static final int height = 30;
    private static final int width = 30;
    private static final int channels = 3;
    private static final int epochs = 50;
    
   private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ConvolutionalNeuralNetwork.class);
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}
	
	public static void newTry(String path, String path2) throws Exception
	{
		 int rngseed = 123;
		 int outputnum = 2;
		Random RandNumGen = new Random(rngseed);
		
		path = "C:\\Users\\Torben\\Desktop\\Datensatz\\train data";
		path2 = "C:\\Users\\Torben\\Desktop\\Datensatz\\test data";
		File trainData = new File(path);
		File testData = new File(path2);
		 
		
		FileSplit train = new FileSplit(trainData,NativeImageLoader.ALLOWED_FORMATS,RandNumGen);
		FileSplit test = new FileSplit(testData,NativeImageLoader.ALLOWED_FORMATS,RandNumGen);

		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		ImageRecordReader recordReader = new ImageRecordReader(height,width,channels,labelMaker);
		
		recordReader.initialize(train);
		//recordReader.setListeners(new LogRecordListener());
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		
		
		DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputnum);
		scaler.fit(dataIter);
		dataIter.setPreProcessor(scaler);
		
	/*	for(int i = 1; i < 3;i++)
		{
			DataSet ds = dataIter.next();
			System.out.println(ds);
			System.out.println(dataIter.getLabels());	
		}*/
		
		LOGGER.info("BUILD MODEL");
		
		MultiLayerNetwork network = alexnetModel(2);
		
		network.init();
		
		network.setListeners(new ScoreIterationListener(10));
		
		
		LOGGER.info("TRAIN MODEL");
		
		for(int i = 0;i < epochscounter;i++)
		{
			
			network.fit(dataIter);
		}
		
		recordReader.reset();
		
		recordReader.initialize(test);
		DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader,batchSize,1, outputnum);
		
		scaler.fit(testIter);
		testIter.setPreProcessor(scaler);
		
		Evaluation eval = new Evaluation(2);
		
		while(testIter.hasNext())
		{
			DataSet next = testIter.next();
			INDArray output = network.output(next.getFeatureMatrix());
			eval.eval(next.getLabels(), output);
		}
		
		LOGGER.info(eval.stats());
		
	}
	public static void load(String path) throws IOException {

		///*LOADING DATA*
		
		// File parentDir = new File(path);
		// FileSplit filesInDir = new FileSplit(parentDir, allowedExtensions, randNumGen);
		 
		// Long numExamples = filesInDir.length();
		 
		 //System.out.print(numExamples);
		 
		 
		 ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		// BalancedPathFilter pathFilter = new BalancedPathFilter(randNumGen, allowedExtensions, labelMaker);


		 
		 rng.nextInt();
		 
		 
		 
		 //ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
	        File mainPath = new File(path); //new File(System.getProperty("user.dir"), "dl4j-examples/src/main/resources/animals/");
	        FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
	       
	        int numExamples = toIntExact(fileSplit.length());
	        int numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length;
	       // numExamples = fileSplit.getRootDir().listFiles(File::isDirectory).length; //This only works if your root is clean: only label subdirs.
	        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels, batchSize);



		 System.out.println("Number of Examples " + numExamples + " Number of Labels :" + numLabels );
			
	        InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
	        InputSplit trainData = inputSplit[0];
	        InputSplit testData = inputSplit[1];
	
	        
	        for(URI u : trainData.locations())
	        {
	        	System.out.println(u.getPath());
	        }
	       
	     System.out.println("TRAIN DATA:" + trainData.length());
		 System.out.println("TEST DATA:" + testData.length());
		 
		    MultiLayerNetwork network = alexnetModel(numLabels);
	        network.init();
		    
	        UIServer uiServer = UIServer.getInstance();
		    StatsStorage statsStorage = new InMemoryStatsStorage();  
		    int listenerFrequency = 1;
		    network.setListeners(new StatsListener(statsStorage, listenerFrequency));
		    uiServer.attach(statsStorage);
		    ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
	        DataSetIterator dataIter;
	        MultipleEpochsIterator trainIter;
	        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
	        
		 for(int i = 0; i < 10; i++)
		 {
			  rng.nextInt();
			  fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
			  numExamples = toIntExact(fileSplit.length());
			  numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length;
			  pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels, batchSize);

			  System.out.println("Number of Examples " + numExamples + " Number of Labels :" + numLabels );
				
		         inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
		         trainData = inputSplit[0];
		         testData = inputSplit[1];
		
		        
		        for(URI u : trainData.locations())
		        {
		        	System.out.println(u.getPath());
		        }

		        
			     System.out.println("TRAIN DATA:" + trainData.length());
				 System.out.println("TEST DATA:" + testData.length());
		        
			       // ImageTransform flipTransform1 = new FlipImageTransform(rng);
			       // ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
			       // ImageTransform warpTransform = new WarpImageTransform(rng, 42);
//			        ImageTransform colorTransform = new ColorConversionTransform(new Random(seed), COLOR_BGR2YCrCb);
			      //<ImageTransform> transforms = Arrays.asList(new ImageTransform[]{flipTransform1, warpTransform, flipTransform2});
			     
			        recordReader.initialize(trainData, null);
			        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
			        scaler.fit(dataIter);
			        dataIter.setPreProcessor(scaler);
			        trainIter = new MultipleEpochsIterator(epochs, dataIter);
			        network.fit(trainIter);

			        
			        recordReader.initialize(testData);
			        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
			        scaler.fit(dataIter);
			        dataIter.setPreProcessor(scaler);
			        Evaluation eval = network.evaluate(dataIter);
			        System.out.println(eval.stats(true));
			        System.out.println(eval.getConfusionMatrix().toString()) ;	        
		 }
		 
		 
		 
		 /* InputSplit[] filesInDirSplit = fileSplit.sample(pathFilter, 80, 20);
		     InputSplit trainData = filesInDirSplit[0];
		     InputSplit testData = filesInDirSplit[1];
		  */
	     
	    // ImageRecordReader recordReader = new ImageRecordReader(height,width,channels,labelMaker);
	     //recordReader.initialize(trainData);
	     //int numberofLabels = recordReader.numLabels();
	 //    int numberofLabels = recordReader.numLabels();
	   //  int outputNum = recordReader.numLabels();
	     
	    /*    ImageTransform flipTransform1 = new FlipImageTransform(rng);
	        ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
	        ImageTransform warpTransform = new WarpImageTransform(rng, 42);
//	        ImageTransform colorTransform = new ColorConversionTransform(new Random(seed), COLOR_BGR2YCrCb);
	        List<ImageTransform> transforms = Arrays.asList(new ImageTransform[]{flipTransform1, warpTransform, flipTransform2});
	     
	        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
	    
	        network.init();
	  /*   
	     int batchSize = 150;
	     int labelIndex = 1;
	     //FileSplit fileSplit = new FileSplit(parentDir, NativeImageLoader.ALLOWED_FORMATS, rng);
	     DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum);

	        UIServer uiServer = UIServer.getInstance();
	       StatsStorage statsStorage = new InMemoryStatsStorage();  
	        
	        
	        
	        
	        //*LOADING MODEL
	       // DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
	        //DataSetIterator  dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum);
	        
	        
	        int listenerFrequency = 1;
	      network.setListeners(new StatsListener(statsStorage, listenerFrequency));
	        uiServer.attach(statsStorage);
	        
	       
	        
	        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
	        DataSetIterator dataIter;
	        MultipleEpochsIterator trainIter;


	        System.out.println("Train model....");
	        // Train without transformations
	        recordReader.initialize(trainData, null);
	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
	        scaler.fit(dataIter);
	        dataIter.setPreProcessor(scaler);
	        trainIter = new MultipleEpochsIterator(epochs, dataIter);
	        network.fit(trainIter);

	        // Train with transformations
	        
	        recordReader.initialize(testData);
	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
	        scaler.fit(dataIter);
	        dataIter.setPreProcessor(scaler);
	        Evaluation eval = network.evaluate(dataIter);
	        System.out.println(eval.stats(true));
	        System.out.println(eval.getConfusionMatrix().toString()) ;
	        
	        
	        
	        
	        
	        for (ImageTransform transform : transforms) {
	            System.out.print("\nTraining on transformation: " + transform.getClass().toString() + "\n\n");
	            recordReader.initialize(trainData, transform);
	            dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
	            scaler.fit(dataIter);
	            dataIter.setPreProcessor(scaler);
	            trainIter = new MultipleEpochsIterator(epochs, dataIter);
	            network.fit(trainIter);
	        }
	        
	        recordReader.initialize(testData);
	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
	        scaler.fit(dataIter);
	        dataIter.setPreProcessor(scaler);
	         eval = network.evaluate(dataIter);
	        System.out.println(eval.stats(true));
	        System.out.println(eval.getConfusionMatrix().toString()) ;
	        
	      //  MultipleEpochsIterator trainIter;
	        
	        
	        */
	        /*
		for(int i = 0; i<2;i++)
		{	
			InputSplit[]  inputSplit = fileSplit.sample(pathFilter, 1.0, 0.0);
		        trainData = inputSplit[0];
		      
		        recordReader.initialize(trainData, null);
		        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
		        scaler.fit(dataIter);
		        dataIter.setPreProcessor(scaler);
		        trainIter = new MultipleEpochsIterator(50, dataIter);

		        
		        network.fit(trainIter);	
		        
		        inputSplit = fileSplit.sample(pathFilter, 0.0, 1.0);
		        testData = inputSplit[1];
		        
		        recordReader.initialize(testData,null);
		        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
		        Evaluation eval = network.evaluate(dataIter);
		       
		        System.out.print(eval.stats());
		        
		}
		
		InputSplit[]  inputSplit = fileSplit.sample(pathFilter, 0.0, 1.0);
        trainData = inputSplit[1];
		
		
		   //80 20
		 
	    
	     //ImageTransform transform = new MultiImageTransform(randNumGen,new ShowImageTransform("Display - before "));
	     
	   
	    // Layer x =  convInit("cnn1", channels, 96, new int[]{11, 11}, new int[]{4, 4}, new int[]{3, 3}, 0);
	      /*  
	        List myList = new ArrayList();
	        Layer[] lay = new Layer[5];
	        for(int i = 1; i < 5;i++)
	        {
	        	System.out.println(network.getLayer(i).getClass());
	           Layer x =  conv3x3("cnn4", 384, 1);
	           lay[i] = x; 
	        } */
	     /*   
	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, 6);
	        scaler.fit(dataIter);
	        dataIter.setPreProcessor(scaler);
	     //   MultipleEpochsIterator trainIter;
	        trainIter = new MultipleEpochsIterator(50, dataIter);
	        
	        network.fit(trainIter);
	      /*  while(dataIter.hasNext()) {
	        DataSet yourData = dataIter.next();
	        yourData.shuffle();
	        //dataIter.
	        network.fit(yourData);
	        }
	        
	        
	        
	        
	        
	        
	        System.out.print("MODEL WURDE TRAINIERT!");
	        
	        
	        recordReader.initialize(testData,null);
	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, 6);
	      //  scaler.fit(dataIter);
	     //  dataIter.setPreProcessor(scaler);
	        
	       // RegressionEvaluation eval = network.evaluateRegression(dataIter);
	       Evaluation eval = network.evaluate(dataIter);
	       
	        System.out.print(eval.stats());
	        
	        
/*	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum);
	        scaler.fit(dataIter);
	        dataIter.setPreProcessor(scaler);
	        Evaluation eval = network.evaluate(dataIter);
	        MultipleEpochsIterator trainIter = new MultipleEpochsIterator(50, dataIter);
	        network.fit(trainIter);
	        
	        recordReader.initialize(testData);
	        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum);
	        scaler.fit(dataIter);
	        dataIter.setPreProcessor(scaler)
	         eval = network.evaluate(dataIter);
	        System.out.print(eval.stats(true));*/
	        
	        
	        
	   /*     UIServer uiServer = UIServer.getInstance();
	        StatsStorage statsStorage = new InMemoryStatsStorage();
	        uiServer.attach(statsStorage);
	        network.setListeners((IterationListener)new StatsListener( statsStorage),new ScoreIterationListener(iterations));
	        /**
	         * Data Setup -> define how to load data into net:
	         *  - recordReader = the reader that loads and converts image data pass in inputSplit to initialize
	         *  - dataIter = a generator that only loads one batch at a time into memory to save memory
	         *  - trainIter = uses MultipleEpochsIterator to ensure model runs through the data for all epochs
	         *

	        // Train without transformations
	        recordReader.initialize(trainData, null);
	        
	        */
	        
	}

	 public static MultiLayerNetwork alexnetModel(int numLabels) {
	        /**
	         * AlexNet model interpretation based on the original paper ImageNet Classification with Deep Convolutional Neural Networks
	         * and the imagenetExample code referenced.
	         * http://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf
	         **/

	        double nonZeroBias = 1;
	        double dropOut = 0.5;

	        ListBuilder listbuild = new NeuralNetConfiguration.Builder()
		            .seed(seed)
		            .weightInit(WeightInit.DISTRIBUTION)
		            .dist(new NormalDistribution(0.0, 0.01))
		            .activation(Activation.RELU)
		            .updater(new Nesterovs(0.9))
		            .iterations(2)
		            .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
		            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		            .learningRate(1e-5)
		            .biasLearningRate(1e-2*2)
		            .learningRateDecayPolicy(LearningRatePolicy.Step)
		            .lrPolicyDecayRate(0.1)
		            .lrPolicySteps(100000)
		            .regularization(true)
		            .l2(5 * 1e-4)
		            .list();
	        
	        
	        for(int i = 1; i <10;i++)
	        {
	        	
	        	listbuild.layer(i, conv3x3("cnn5", 256, nonZeroBias));
	        	
	        }
	        
	        
	        
	        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .seed(seed)
	            .weightInit(WeightInit.DISTRIBUTION)
	            .dist(new NormalDistribution(0.0, 0.01))
	           // .weightInit(WeightInit.)
	            .activation(Activation.RELU)
	            .updater(new Nesterovs(0.9))
	            .iterations(2)
	            .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .learningRate(1e-3)
	            .biasLearningRate(1e-2*2)
	            //.learningRateDecayPolicy(LearningRatePolicy.Step)
	            .learningRateDecayPolicy(LearningRatePolicy.Step)
	            .lrPolicyDecayRate(0.1)
	            .lrPolicySteps(100000)
	            .regularization(true)
	            .l2(5 * 1e-4)
	            .list()	        
	            .layer(0, convInit("cnn1", channels, 96, new int[]{11, 11}, new int[]{4, 4}, new int[]{3, 3}, 0))
	            .layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
	            .layer(2, maxPool("maxpool1", new int[]{3,3}))
	            .layer(3, conv5x5("cnn2", 256, new int[] {1,1}, new int[] {2,2}, nonZeroBias))
	            .layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
	            .layer(5, maxPool("maxpool2", new int[]{3,3}))
	            .layer(6,conv3x3("cnn3", 384, 0))
	           // .layer(6,conv3x3("cnn4", 384, nonZeroBias))
	           // .layer(7,conv3x3("cnn5", 256, nonZeroBias))
	            //.layer(7, maxPool("maxpool3", new int[]{3,3}))
	            .layer(7, fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
	            //.layer(9, fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
	            //.layer(8, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	            .layer(8, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	                .name("output")
	                .nOut(2)
	                .activation(Activation.SOFTMAX)
	                .build())
	            .backprop(true)
	            .pretrain(false)
	            .setInputType(InputType.convolutional(height, width, channels))
	            .build();
	        
	       
	        
	        return new MultiLayerNetwork(conf);

	    }
	 
	    private static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
	        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
	    }

	    private static ConvolutionLayer conv3x3(String name, int out, double bias) {
	        return new ConvolutionLayer.Builder(new int[]{3,3}, new int[] {1,1}, new int[] {1,1}).name(name).nOut(out).biasInit(bias).build();
	    }

	    private static ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
	        return new ConvolutionLayer.Builder(new int[]{5,5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
	    }

	    private static SubsamplingLayer maxPool(String name,  int[] kernel) {
	        return new SubsamplingLayer.Builder(kernel, new int[]{2,2}).name(name).build();
	    }

	    private static DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
	        return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
	    }
}
