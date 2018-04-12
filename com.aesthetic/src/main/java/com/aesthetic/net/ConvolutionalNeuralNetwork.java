package com.aesthetic.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.spi.LoggerFactory;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.api.transform.condition.column.NaNColumnCondition;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingModelSaver;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.InvalidScoreIterationTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.ZooModel;
import org.iq80.leveldb.impl.Filename;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;

import com.aesthetic.gui.ProgressGui;
import com.aesthetic.main.DBHelper;

import akka.contrib.pattern.ClusterClient.Publish;

import org.deeplearning4j.zoo.model.AlexNet;
import org.deeplearning4j.zoo.model.GoogLeNet;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import views.html.helper.textarea;

import static java.lang.Math.toIntExact;

import java.awt.image.BufferedImage;

public class ConvolutionalNeuralNetwork extends SwingWorker<Void, String> {
	private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;

	protected static long seed = 42;
	protected static Random rng = new Random(seed);
	protected static double splitTrainTest = 0.8;
	private static int batchSize = 60;
	
	
	public static int getBatchSize() {
		return batchSize;
	}

	public static void setBatchSize(int batchSize) {
		ConvolutionalNeuralNetwork.batchSize = batchSize;
	}

	private int epochscounter = 30;
	private int cnn_min = 1;
	private int cnn_max = 5;
	// private static final long seed = 12345;

	private static final Random randNumGen = new Random(seed);

	private static int height = 224;
	private static int width = 224;
	private static int channels = 3;
	private static int epochs = 50;
	private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ConvolutionalNeuralNetwork.class);
	private static Model bestNetwork = null;
	private static double accuracy = 0;

	private JTextArea JDP;
	private JLabel jlabel;
	private String train_path = "";
	private String test_path = "";
	private String output_path = "";
	private NetworkType networkType;
	private boolean showinbrowser = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ConvolutionalNeuralNetwork(String train, String test, String out, ProgressGui proggui, NetworkType nettype,
			boolean showinb) {
		JDP = proggui.getTextArea();
		jlabel = proggui.getLblWert();
		train_path = train;
		test_path = test;
		output_path = out;
		networkType = nettype;
		showinbrowser = showinb;
	}

	public static MultiLayerNetwork Kao() {
		Map<Integer, Double> lrSchedule = new HashMap<>();
		lrSchedule.put(0, 0.1); // iteration #, learning rate
		lrSchedule.put(500, 0.01);
		lrSchedule.put(1000, 0.001);
		lrSchedule.put(1500, 0.0001);
		lrSchedule.put(2000, 0.00001);

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(1).inferenceWorkspaceMode(WorkspaceMode.SINGLE).trainingWorkspaceMode(WorkspaceMode.SINGLE)
				.regularization(false).l2(0.0005).learningRate(.01).learningRateDecayPolicy(LearningRatePolicy.Schedule)
				.learningRateSchedule(lrSchedule) // overrides the rate set in learningRate
				.weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(0.0, 0.01))
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).updater(Updater.NESTEROVS).list()
				.layer(0,
						new ConvolutionLayer.Builder(7, 7)
								.nIn(3).stride(2, 2).nOut(96).activation(Activation.RELU).build())
				.layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
				.layer(2,
						new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
								.kernelSize(3, 3).stride(2, 2).build())
				.layer(3, new ConvolutionLayer.Builder(5, 5).stride(1, 1) // nIn need not specified in later layers
						.nOut(96)
						// .activation(Activation.IDENTITY)
						.build())
				.layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
				.layer(5, new SubsamplingLayer.Builder().kernelSize(3, 3).stride(2, 2).build())
				.layer(6, new ConvolutionLayer.Builder(3, 3).stride(1, 1) // nIn need not specified in later layers
						.nOut(96)
						// .activation(Activation.IDENTITY)
						.build())
				.layer(7, new ConvolutionLayer.Builder(3, 3).stride(1, 1) // nIn need not specified in later layers
						.nOut(96)
						// .activation(Activation.IDENTITY)
						.build())
				.layer(8, new ConvolutionLayer.Builder(3, 3).stride(1, 1) // nIn need not specified in later layers
						.nOut(96)
						// .activation(Activation.IDENTITY)
						.build())
				.layer(9, new LocalResponseNormalization.Builder().name("lrn3").build())
				.layer(10, new SubsamplingLayer.Builder().kernelSize(3, 3).stride(2, 2).build())
				.layer(11, new DenseLayer.Builder().activation(Activation.RELU).nOut(1024).build())
				.layer(12, new DenseLayer.Builder().activation(Activation.RELU).nOut(1024).build())
				.layer(13,
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(2)
								.activation(Activation.SOFTMAX).build())
				.setInputType(InputType.convolutional(height, width, 3)) // InputType.convolutional for normal image
				.backprop(true).pretrain(false).build();

		//Nd4j.getMemoryManager().setAutoGcWindow(5000);
		
		return new MultiLayerNetwork(conf);

	}

	public static MultiLayerNetwork newNetwork() {
		Map<Integer, Double> lrSchedule = new HashMap<>();
		lrSchedule.put(0, 1e-5); // iteration #, learning rate
		lrSchedule.put(200, 0.05);
		lrSchedule.put(600, 0.028);
		lrSchedule.put(800, 0.0060);
		lrSchedule.put(1000, 0.001);

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(1)
				.regularization(true).l2(0.0005).learningRate(.01).learningRateDecayPolicy(LearningRatePolicy.Schedule)
				.learningRateSchedule(lrSchedule) // overrides the rate set in learningRate
				.weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(0.0, 0.01))
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).updater(Updater.NESTEROVS).list()
				.layer(0,
						new ConvolutionLayer.Builder(5, 5).nIn(channels).stride(1, 1).nOut(20)
								.activation(Activation.IDENTITY).build())
				.layer(1,
						new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
								.kernelSize(2, 2).stride(2, 2).build())
				.layer(2, new ConvolutionLayer.Builder(5, 5).stride(1, 1) // nIn need not specified in later layers
						.nOut(50).activation(Activation.IDENTITY).build())
				.layer(3,
						new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2)
								.build())
				.layer(4, new DenseLayer.Builder().activation(Activation.RELU).nOut(500).build())
				.layer(5,
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(2)
								.activation(Activation.SOFTMAX).build())
				.setInputType(InputType.convolutionalFlat(height, width, 3)) // InputType.convolutional for normal image
				.backprop(true).pretrain(false).build();

		return new MultiLayerNetwork(conf);

	}

	public static void addLabelsToZipFolder(List<String> labels, File model) throws Exception {

		if (!model.exists()) {
			return;
		}

		ZipFile zipFile = new ZipFile(model);
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		File labelfile = new File("label.txt");
		int labelcount = 0;
		while (labelfile.exists()) {
			labelfile = new File("label" + labelcount + ".txt");
			labelcount++;
		}

		try (PrintWriter out = new PrintWriter(labelfile)) {
			for (String s : labels) {
				out.println(s);
			}
			out.close();

		}

		ArrayList<File> newFile = new ArrayList<File>();
		newFile.add(labelfile);

		zipFile.addFiles(newFile, parameters);
		labelfile.delete();

	}

	public static List<String> getLabelsFromModelZip(File model) throws Exception {
		final java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(model);

		FileInputStream fis = new FileInputStream(model);
		BufferedInputStream bis = new BufferedInputStream(fis);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipInputStream zis = new ZipInputStream(bis);

		List<String> result = new ArrayList<String>();

		ZipEntry ze;

		while ((ze = zis.getNextEntry()) != null) {

			if (!ze.isDirectory()) {
				final String fileName = ze.getName();
				if (fileName.endsWith(".txt") && fileName.contains("label")) {

					InputStream input = zis;
					BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
					String line;
					while ((line = br.readLine()) != null) {
						result.add(line);
					}
					br.close();
					zis.close();
					return result;
				}
			}
		}

		zis.close();
		return null;
	}

	public static void load(String path) throws IOException {

		/// *LOADING DATA*

		// File parentDir = new File(path);
		// FileSplit filesInDir = new FileSplit(parentDir, allowedExtensions,
		// randNumGen);

		// Long numExamples = filesInDir.length();

		// System.out.print(numExamples);

		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		// BalancedPathFilter pathFilter = new BalancedPathFilter(randNumGen,
		// allowedExtensions, labelMaker);

		rng.nextInt();

		// ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		File mainPath = new File(path); // new File(System.getProperty("user.dir"),
										// "dl4j-examples/src/main/resources/animals/");
		FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);

		int numExamples = toIntExact(fileSplit.length());
		int numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length;
		// numExamples = fileSplit.getRootDir().listFiles(File::isDirectory).length;
		// //This only works if your root is clean: only label subdirs.
		BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels, batchSize);

		System.out.println("Number of Examples " + numExamples + " Number of Labels :" + numLabels);

		InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
		InputSplit trainData = inputSplit[0];
		InputSplit testData = inputSplit[1];

		for (URI u : trainData.locations()) {
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

		for (int i = 0; i < 10; i++) {
			rng.nextInt();
			fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
			numExamples = toIntExact(fileSplit.length());
			numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length;
			pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels, batchSize);

			System.out.println("Number of Examples " + numExamples + " Number of Labels :" + numLabels);

			inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
			trainData = inputSplit[0];
			testData = inputSplit[1];

			for (URI u : trainData.locations()) {
				System.out.println(u.getPath());
			}

			System.out.println("TRAIN DATA:" + trainData.length());
			System.out.println("TEST DATA:" + testData.length());

			// ImageTransform flipTransform1 = new FlipImageTransform(rng);
			// ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
			// ImageTransform warpTransform = new WarpImageTransform(rng, 42);
			// ImageTransform colorTransform = new ColorConversionTransform(new
			// Random(seed), COLOR_BGR2YCrCb);
			// <ImageTransform> transforms = Arrays.asList(new
			// ImageTransform[]{flipTransform1, warpTransform, flipTransform2});

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
			System.out.println(eval.getConfusionMatrix().toString());
		}

		/*
		 * InputSplit[] filesInDirSplit = fileSplit.sample(pathFilter, 80, 20);
		 * InputSplit trainData = filesInDirSplit[0]; InputSplit testData =
		 * filesInDirSplit[1];
		 */

		// ImageRecordReader recordReader = new
		// ImageRecordReader(height,width,channels,labelMaker);
		// recordReader.initialize(trainData);
		// int numberofLabels = recordReader.numLabels();
		// int numberofLabels = recordReader.numLabels();
		// int outputNum = recordReader.numLabels();

		/*
		 * ImageTransform flipTransform1 = new FlipImageTransform(rng); ImageTransform
		 * flipTransform2 = new FlipImageTransform(new Random(123)); ImageTransform
		 * warpTransform = new WarpImageTransform(rng, 42); // ImageTransform
		 * colorTransform = new ColorConversionTransform(new Random(seed),
		 * COLOR_BGR2YCrCb); List<ImageTransform> transforms = Arrays.asList(new
		 * ImageTransform[]{flipTransform1, warpTransform, flipTransform2});
		 * 
		 * DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		 * 
		 * network.init(); /* int batchSize = 150; int labelIndex = 1; //FileSplit
		 * fileSplit = new FileSplit(parentDir, NativeImageLoader.ALLOWED_FORMATS, rng);
		 * DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader,
		 * batchSize, labelIndex, outputNum);
		 * 
		 * UIServer uiServer = UIServer.getInstance(); StatsStorage statsStorage = new
		 * InMemoryStatsStorage();
		 * 
		 * 
		 * 
		 * 
		 * //*LOADING MODEL // DataNormalization scaler = new
		 * ImagePreProcessingScaler(0, 1); //DataSetIterator dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum);
		 * 
		 * 
		 * int listenerFrequency = 1; network.setListeners(new
		 * StatsListener(statsStorage, listenerFrequency));
		 * uiServer.attach(statsStorage);
		 * 
		 * 
		 * 
		 * ImageRecordReader recordReader = new ImageRecordReader(height, width,
		 * channels, labelMaker); DataSetIterator dataIter; MultipleEpochsIterator
		 * trainIter;
		 * 
		 * 
		 * System.out.println("Train model...."); // Train without transformations
		 * recordReader.initialize(trainData, null); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
		 * scaler.fit(dataIter); dataIter.setPreProcessor(scaler); trainIter = new
		 * MultipleEpochsIterator(epochs, dataIter); network.fit(trainIter);
		 * 
		 * // Train with transformations
		 * 
		 * recordReader.initialize(testData); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
		 * scaler.fit(dataIter); dataIter.setPreProcessor(scaler); Evaluation eval =
		 * network.evaluate(dataIter); System.out.println(eval.stats(true));
		 * System.out.println(eval.getConfusionMatrix().toString()) ;
		 * 
		 * 
		 * 
		 * 
		 * 
		 * for (ImageTransform transform : transforms) {
		 * System.out.print("\nTraining on transformation: " +
		 * transform.getClass().toString() + "\n\n"); recordReader.initialize(trainData,
		 * transform); dataIter = new RecordReaderDataSetIterator(recordReader,
		 * batchSize, 1, numLabels); scaler.fit(dataIter);
		 * dataIter.setPreProcessor(scaler); trainIter = new
		 * MultipleEpochsIterator(epochs, dataIter); network.fit(trainIter); }
		 * 
		 * recordReader.initialize(testData); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
		 * scaler.fit(dataIter); dataIter.setPreProcessor(scaler); eval =
		 * network.evaluate(dataIter); System.out.println(eval.stats(true));
		 * System.out.println(eval.getConfusionMatrix().toString()) ;
		 * 
		 * // MultipleEpochsIterator trainIter;
		 * 
		 * 
		 */
		/*
		 * for(int i = 0; i<2;i++) { InputSplit[] inputSplit =
		 * fileSplit.sample(pathFilter, 1.0, 0.0); trainData = inputSplit[0];
		 * 
		 * recordReader.initialize(trainData, null); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
		 * scaler.fit(dataIter); dataIter.setPreProcessor(scaler); trainIter = new
		 * MultipleEpochsIterator(50, dataIter);
		 * 
		 * 
		 * network.fit(trainIter);
		 * 
		 * inputSplit = fileSplit.sample(pathFilter, 0.0, 1.0); testData =
		 * inputSplit[1];
		 * 
		 * recordReader.initialize(testData,null); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
		 * Evaluation eval = network.evaluate(dataIter);
		 * 
		 * System.out.print(eval.stats());
		 * 
		 * }
		 * 
		 * InputSplit[] inputSplit = fileSplit.sample(pathFilter, 0.0, 1.0); trainData =
		 * inputSplit[1];
		 * 
		 * 
		 * //80 20
		 * 
		 * 
		 * //ImageTransform transform = new MultiImageTransform(randNumGen,new
		 * ShowImageTransform("Display - before "));
		 * 
		 * 
		 * // Layer x = convInit("cnn1", channels, 96, new int[]{11, 11}, new int[]{4,
		 * 4}, new int[]{3, 3}, 0); /* List myList = new ArrayList(); Layer[] lay = new
		 * Layer[5]; for(int i = 1; i < 5;i++) {
		 * System.out.println(network.getLayer(i).getClass()); Layer x = conv3x3("cnn4",
		 * 384, 1); lay[i] = x; }
		 */
		/*
		 * dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, 6);
		 * scaler.fit(dataIter); dataIter.setPreProcessor(scaler); //
		 * MultipleEpochsIterator trainIter; trainIter = new MultipleEpochsIterator(50,
		 * dataIter);
		 * 
		 * network.fit(trainIter); /* while(dataIter.hasNext()) { DataSet yourData =
		 * dataIter.next(); yourData.shuffle(); //dataIter. network.fit(yourData); }
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * System.out.print("MODEL WURDE TRAINIERT!");
		 * 
		 * 
		 * recordReader.initialize(testData,null); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, 1, 6); //
		 * scaler.fit(dataIter); // dataIter.setPreProcessor(scaler);
		 * 
		 * // RegressionEvaluation eval = network.evaluateRegression(dataIter);
		 * Evaluation eval = network.evaluate(dataIter);
		 * 
		 * System.out.print(eval.stats());
		 * 
		 * 
		 * /* dataIter = new RecordReaderDataSetIterator(recordReader, batchSize,
		 * labelIndex, outputNum); scaler.fit(dataIter);
		 * dataIter.setPreProcessor(scaler); Evaluation eval =
		 * network.evaluate(dataIter); MultipleEpochsIterator trainIter = new
		 * MultipleEpochsIterator(50, dataIter); network.fit(trainIter);
		 * 
		 * recordReader.initialize(testData); dataIter = new
		 * RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum);
		 * scaler.fit(dataIter); dataIter.setPreProcessor(scaler) eval =
		 * network.evaluate(dataIter); System.out.print(eval.stats(true));
		 */

		/*
		 * UIServer uiServer = UIServer.getInstance(); StatsStorage statsStorage = new
		 * InMemoryStatsStorage(); uiServer.attach(statsStorage);
		 * network.setListeners((IterationListener)new StatsListener( statsStorage),new
		 * ScoreIterationListener(iterations)); /** Data Setup -> define how to load
		 * data into net: - recordReader = the reader that loads and converts image data
		 * pass in inputSplit to initialize - dataIter = a generator that only loads one
		 * batch at a time into memory to save memory - trainIter = uses
		 * MultipleEpochsIterator to ensure model runs through the data for all epochs
		 *
		 * 
		 * // Train without transformations recordReader.initialize(trainData, null);
		 * 
		 */

	}

	public static HashMap<String, String> Try_model(File model, File[] images) throws Exception {
		//// LOAD MODEL
		Nd4j.getRandom().setSeed(12345);
		MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(model);
		HashMap<String, String> files = new HashMap<String, String>();
		List<String> labels = getLabelsFromModelZip(model);

		if (labels != null) {
			int counter = 0;
			for (String s : labels) {
				files.put("Klasse " + s, " entspricht " + counter);
				counter++;
			}

		}

		for (File file : images) {
			NativeImageLoader loader = new NativeImageLoader(height, width, 3);
			INDArray image = null;
			try {
				image = loader.asMatrix(file);
			} catch (Exception e) {
				LOGGER.error("the loader.asMatrix occured an error", e);
			}
			// LOGGER.info(network.getLabels().toString());
			INDArray output = network.output(image);

			String introduction;

			int[] predict = network.predict(image);
			String modelResult = "Prediction: ";

			modelResult = modelResult + labels.get(predict[0]);

			/*
			 * for(int i = 0; i < predict.length;i++) { if(i < labels.size()) { modelResult
			 * = modelResult + labels.get(i) + ":"; }
			 * 
			 * 
			 * modelResult = modelResult + predict[i];
			 * 
			 * if(i<predict.length-1) { modelResult = modelResult + predict[i] + ";"; }
			 */

			if (labels == null) {
				modelResult = output.toString();
				modelResult += "===" + Arrays.toString(predict);
			}
			//
			//

			files.put(file.getPath(), modelResult);
		}

		return files;
	}

	public static MultiLayerNetwork alexnetModel(int numLabels) {
		/**
		 * AlexNet model interpretation based on the original paper ImageNet
		 * Classification with Deep Convolutional Neural Networks and the
		 * imagenetExample code referenced.
		 * http://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf
		 **/

		double nonZeroBias = 1;
		double dropOut = 0.5;

		ListBuilder listbuild = new NeuralNetConfiguration.Builder().seed(seed).weightInit(WeightInit.DISTRIBUTION)
				.dist(new NormalDistribution(0.0, 0.01)).activation(Activation.RELU).updater(new Nesterovs(0.9))
				.iterations(2).gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to
																									// prevent vanishing
																									// or exploding
																									// gradients
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).learningRate(1e-4)
				.biasLearningRate(1e-2 * 2).learningRateDecayPolicy(LearningRatePolicy.Step).lrPolicyDecayRate(0.1)
				.lrPolicySteps(100000).regularization(true).l2(1e-4).list();

		for (int i = 1; i < 10; i++) {

			listbuild.layer(i, conv3x3("cnn5", 256, nonZeroBias));

		}

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).weightInit(WeightInit.XAVIER)
				// .weightInit(WeightInit.DISTRIBUTION)
				// .dist(new NormalDistribution(0.0, 0.01))
				// .weightInit(WeightInit.)
				// .activation(Activation.RELU)
				// .updater(new Nesterovs(0.9))
				.updater(Updater.NESTEROVS).iterations(1)
				// .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) //
				// normalize to prevent vanishing or exploding gradients
				.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).learningRate(1e-5)
				.biasLearningRate(1e-2 * 2)
				// .learningRateDecayPolicy(LearningRatePolicy.Step)
				.learningRateDecayPolicy(LearningRatePolicy.Step).lrPolicyDecayRate(0.1).lrPolicySteps(100000)
				.regularization(true).l2(5 * 1e-4).list()
				.layer(0,
						convInit("cnn1", channels, 96, new int[] { 11, 11 }, new int[] { 4, 4 }, new int[] { 3, 3 }, 0))
				.layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
				.layer(2, maxPool("maxpool1", new int[] { 3, 3 }))
				.layer(3, conv5x5("cnn2", 256, new int[] { 1, 1 }, new int[] { 2, 2 }, nonZeroBias))
				.layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
				.layer(5, maxPool("maxpool2", new int[] { 3, 3 })).layer(6, conv3x3("cnn3", 384, 0))
				.layer(7, conv3x3("cnn4", 384, nonZeroBias)).layer(8, conv3x3("cnn5", 256, nonZeroBias))
				// .layer(9, maxPool("maxpool3", new int[]{3,3}))
				.layer(9, fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
				// .layer(9, fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new
				// GaussianDistribution(0, 0.005)))
				// .layer(8, new
				// OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
				.layer(10,
						new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT).name("output").nOut(2)
								.activation(Activation.SOFTMAX).build())
				.backprop(true).pretrain(false).setInputType(InputType.convolutional(height, width, channels)).build();

		return new MultiLayerNetwork(conf);

	}

	public static MultiLayerNetwork lenetModel() {
		/**
		 * Revisde Lenet Model approach developed by ramgo2 achieves slightly above
		 * random Reference:
		 * https://gist.github.com/ramgo2/833f12e92359a2da9e5c2fb6333351c5
		 **/
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(1)
				.regularization(false).l2(0.005) // tried 0.0001, 0.0005
				.activation(Activation.RELU).learningRate(0.0001) // tried 0.00001, 0.00005, 0.000001
				.weightInit(WeightInit.XAVIER).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.updater(new Nesterovs(0.9)).list()
				.layer(0, convInit("cnn1", channels, 50, new int[] { 5, 5 }, new int[] { 1, 1 }, new int[] { 0, 0 }, 0))
				.layer(1, maxPool("maxpool1", new int[] { 2, 2 }))
				.layer(2, conv5x5("cnn2", 100, new int[] { 5, 5 }, new int[] { 1, 1 }, 0))
				.layer(3, maxPool("maxool2", new int[] { 2, 2 })).layer(4, new DenseLayer.Builder().nOut(500).build())
				.layer(5,
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(2)
								.activation(Activation.SOFTMAX).build())
				.backprop(true).pretrain(false).setInputType(InputType.convolutional(height, width, channels)).build();

		return new MultiLayerNetwork(conf);

	}

	private static MultiLayerNetwork own(int amount_conv_layer, WeightInit weight, OptimizationAlgorithm algo) {
		double nonZeroBias = 0;// 1;
		double dropOut = 0.5;

		// ZU probierende Learningrates
		Map<Integer, Double> lrSchedule = new HashMap<>();
		lrSchedule.put(1, 0.05);
		lrSchedule.put(200, 0.01);
		lrSchedule.put(300, 0.001);
		lrSchedule.put(500, 0.0001);
		lrSchedule.put(600, 0.00001);
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		builder.seed(seed);
		// builder.weightInit(weight);
		builder.weightInit(WeightInit.RELU);
		builder.activation(Activation.RELU);
		// builder.setConvolutionMode(ConvolutionMode.Same);
		// builder.setMiniBatch(miniBatch);
		// builder.setUseRegularization(true);
		builder.regularization(false).l2(0.0005);
		//builder.convolutionMode(ConvolutionMode.Same);
		// if(weight == WeightInit.DISTRIBUTION)
		// {
		// builder.dist(new NormalDistribution(0.0, 0.01));
		// }
		builder.inferenceWorkspaceMode(WorkspaceMode.SINGLE);
		builder.trainingWorkspaceMode(WorkspaceMode.SINGLE);
		builder.iterations(1);
		builder.learningRate(0.001);
		//builder.learningRateDecayPolicy(LearningRatePolicy.Schedule);
		//builder.learningRateSchedule(lrSchedule);
		// builder.updater(Updater.NESTEROVS);
		builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
		builder.updater(new Nesterovs(0.9));
		// builder.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer);
		// builder.l2(1e-3);
		// builder.dropOut(0.5);

		// if(algo != null)
		// builder.optimizationAlgo(algo);
		//
		ListBuilder listbuilder = builder.list();

		/*
		 * ListBuilder listbuilder = new NeuralNetConfiguration.Builder() .seed(seed)
		 * //.weightInit(WeightInit.XAVIER) //.weightInit(WeightInit.RELU)
		 * .weightInit(weight) .dist(new NormalDistribution(0.0, 0.01))
		 * 
		 * //.activation(Activation.RELU) //.updater(new Nesterovs(0.9))
		 * //.updater(Updater.NESTEROVS) //.updater(Updater. .iterations(1) //
		 * .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) //
		 * normalize to prevent vanishing or exploding gradients
		 * //.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
		 * //.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		 * //.learningRate(0.01) .learningRateDecayPolicy(LearningRatePolicy.Schedule)
		 * .learningRateSchedule(lrSchedule) // .biasLearningRate(1e-2*2) //
		 * .learningRateDecayPolicy(LearningRatePolicy.Step)
		 * //.learningRateDecayPolicy(LearningRatePolicy.Step) //
		 * .lrPolicyDecayRate(0.1) // .lrPolicySteps(100000) //.regularization(true) //
		 * .l2(5 * 1e-4) .list();
		 */
		listbuilder.layer(0,
				convInit("cnn1", channels, 50, new int[] { 7, 7 }, new int[] { 1, 1 }, new int[] { 0, 0 }, 0));
		// listbuilder.layer(1, new
		// LocalResponseNormalization.Builder().name("lrn1").build());
		listbuilder.layer(1, maxPool("maxpool1", new int[] { 2, 2 }));
		listbuilder.layer(2, conv5x5("cnn" + 2, 64, new int[] { 5, 5 }, new int[] { 0, 0 }, nonZeroBias));
		int counter = 0;
		int cnncounter = 3;
		for (int i = 1; i <= amount_conv_layer; i++) {

			// listbuilder.layer(i+2, conv3x3("cnn"+i+2, 256, new int[] {1,1}, new int[]
			// {2,2}, nonZeroBias));
			listbuilder.layer(cnncounter, conv3x3("cnn" + i + 2, 64, nonZeroBias));
			cnncounter++;
			// listbuilder.layer(i+1, conv5x5("cnn"+i+2,100,new int[] {5,5},new int[]
			// {0,0},nonZeroBias));
			// cnncounter = cnncounter+4;
			// counter = i+4;
		}
		// 4096
		// cnncounter++;
		listbuilder.layer(cnncounter, new LocalResponseNormalization.Builder().name("lrn2" + cnncounter).build());
		cnncounter++;
		listbuilder.layer(cnncounter, maxPool("maxpool" + cnncounter, new int[] { 2, 2 }));
		cnncounter++;
		counter = cnncounter;
		listbuilder.layer(counter,
				fullyConnected("ffn" + counter, 500, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)));
		listbuilder.layer(counter+1, fullyConnected("ffn" + counter+1, 256,
		 nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)));

		listbuilder.layer(counter + 1,
				new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).name("output").nOut(2)
						// .nIn(256)
						.activation(Activation.SOFTMAX).build());
		listbuilder.backprop(true);
		listbuilder.pretrain(false);
		listbuilder.setInputType(InputType.convolutional(height, width, channels));
		MultiLayerConfiguration conf_test = listbuilder.build();

		return new MultiLayerNetwork(conf_test);
		/*
		 * MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
		 * .seed(seed) //.weightInit(WeightInit.XAVIER) //.weightInit(WeightInit.RELU)
		 * .weightInit(WeightInit.DISTRIBUTION) .dist(new NormalDistribution(0.0, 0.01))
		 * 
		 * //.activation(Activation.RELU) //.updater(new Nesterovs(0.9))
		 * //.updater(Updater.NESTEROVS) //.updater(Updater. .iterations(1) //
		 * .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) //
		 * normalize to prevent vanishing or exploding gradients
		 * //.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
		 * //.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		 * .learningRate(0.01) // .biasLearningRate(1e-2*2) //
		 * .learningRateDecayPolicy(LearningRatePolicy.Step)
		 * //.learningRateDecayPolicy(LearningRatePolicy.Step) //
		 * .lrPolicyDecayRate(0.1) // .lrPolicySteps(100000) //.regularization(true) //
		 * .l2(5 * 1e-4) .list() .layer(0, convInit("cnn1", channels, 100, new int[]{11,
		 * 11}, new int[]{3, 3}, new int[]{3, 3}, 0)) .layer(1, new
		 * LocalResponseNormalization.Builder().name("lrn1").build()) .layer(2,
		 * maxPool("maxpool1", new int[]{3,3})) .layer(3, conv5x5("cnn2", 256, new int[]
		 * {1,1}, new int[] {2,2}, nonZeroBias)) .layer(4, new
		 * LocalResponseNormalization.Builder().name("lrn2").build()) .layer(5,
		 * maxPool("maxpool2", new int[]{3,3})) .layer(6,conv3x3("cnn3", 384, 0))
		 * .layer(7,conv3x3("cnn4", 384, nonZeroBias)) .layer(8,conv3x3("cnn5", 256,
		 * nonZeroBias)) .layer(9, maxPool("maxpool3", new int[]{1,1}))
		 * .layer(10,conv3x3("cnn6", 256, nonZeroBias)) .layer(11, maxPool("maxpool3",
		 * new int[]{1,1})) .layer(12,conv3x3("cnn7", 256, nonZeroBias)) .layer(13,
		 * maxPool("maxpool4", new int[]{1,1})) .layer(14, fullyConnected("ffn1", 4096,
		 * nonZeroBias, dropOut, new GaussianDistribution(0, 0.005))) .layer(15,
		 * fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new
		 * GaussianDistribution(0, 0.005))) //.layer(8, new
		 * OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
		 * .layer(16, new
		 * OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
		 * .name("output") .nOut(2) .activation(Activation.SOFTMAX) .build())
		 * .backprop(true) .pretrain(false)
		 * .setInputType(InputType.convolutional(height, width, channels)) .build();
		 * 
		 * 
		 * 
		 * 
		 * MultiLayerNetwork result = new MultiLayerNetwork(conf);
		 * 
		 * 
		 * 
		 * return result;
		 */
	}

	private static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad,
			double bias) {
		return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
	}

	private static ConvolutionLayer conv3x3(String name, int out, double bias) {
		// HIER RELU EINGEFÜGT --> notfalls löschen
		return new ConvolutionLayer.Builder(new int[] { 3, 3 }, new int[] { 1, 1 }, new int[] { 1, 1 }).name(name)
				.nOut(out).biasInit(bias).build();
	}

	private static ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
		return new ConvolutionLayer.Builder(new int[] { 5, 5 }, stride, pad).name(name).nOut(out).biasInit(bias)
				.build();
	}

	private static SubsamplingLayer maxPool(String name, int[] kernel) {
		return new SubsamplingLayer.Builder(kernel, new int[] { 2, 2 }).name(name)
				.poolingType(SubsamplingLayer.PoolingType.MAX).build();
	}

	private static DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
		// dist(dist)
		return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).activation(Activation.RELU)
				.build();
	}

	@Override
	protected Void doInBackground() throws Exception {
		int rngseed = 123;
		int outputnum = 2;
		Random RandNumGen = new Random(rngseed);
		String path_model = output_path;// "C:\\Users\\Torben\\Desktop\\Small Dataset\\Models\\";
		// path = "C:\\Users\\Torben\\Desktop\\New Small Dataset\\train data";
		// path2 = "C:\\Users\\Torben\\Desktop\\New Small Dataset\\test data";
		File trainData = new File(train_path);
		File testData = new File(test_path);
		UIServer uiServer = null;
		StatsStorage statsStorage = null;
		List<String> labels = new ArrayList<String>();
		boolean extractedformat = false;
		for (File f : trainData.listFiles()) {
			if (f.isDirectory()) {
				labels.add(f.getName());

				/// WENN DAS VERZEICHNIS SAUBER IST --> Checke files nach Image im ersten
				/// Versuch
				// und setze höhe und Breite --> Annahme alle bilder haben gleiche größe!
				if (extractedformat == false) {

					for (File x : f.listFiles()) {
						if (x.isFile()) {
							BufferedImage buf = null;
							if ((buf = ImageIO.read(x)) != null) {
								width = buf.getWidth();
								height = buf.getHeight();
								extractedformat = true;
								buf.flush();
								buf = null;
								break;
							} else {
								/// LEAVE IT
								buf = null;
								break;
							}
						}
					}
				}
			}

		}
		java.util.Collections.sort(labels);


		publish("START EVALUATION!");
		/*
		 * for(int i = 1; i < 3;i++) { DataSet ds = dataIter.next();
		 * System.out.println(ds); System.out.println(dataIter.getLabels()); }
		 */

		LOGGER.info("BUILD MODEL");

		// MultiLayerNetwork network = alexnetModel(2);
		// MultiLayerNetwork network = newNetwork();
		MultiLayerNetwork network = null;
		ComputationGraph googlenet = null;
		if (networkType == NetworkType.AlexNet) {
			AlexNet zooModel = new org.deeplearning4j.zoo.model.AlexNet(2, seed, 1);
			network = new MultiLayerNetwork(zooModel.conf());
			network.init();
		} else if (networkType == NetworkType.GoogleNet) {
			// googlenet = new ComputationGraph(new
			// org.deeplearning4j.zoo.model.GoogLeNet(2, seed, 1).conf());
			googlenet = new ComputationGraph(new com.aesthetic.net.GoogLeNet(2, seed).conf());
			googlenet.init();
		}
		else if(networkType == NetworkType.Kao)
		{
			network = Kao();
		}

		
		
		
		// zooModel.init();

		// File model = new
		// File("C:\\Users\\Torben\\Downloads\\googlenet_dl4j_inference.zip");

		// ComputationGraph network = ModelSerializer.restoreComputationGraph(model);

		File dirFile = new File(output_path);

		dirFile.mkdir();

		/// TRY Different Stuff
		WeightInit[] actv = new WeightInit[1];

		// actv[0] = WeightInit.DISTRIBUTION;
		// actv[1] = WeightInit.XAVIER;
		OptimizationAlgorithm[] algo = new OptimizationAlgorithm[1];
		algo[0] = null;
		// algo[1] = OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;

		/// ANZAHL SCHICHTEN
		for (int i = cnn_min; i <= cnn_max; i++) {
			
			publish("Number of additional Layers:" + i);

			// VORERST OHNE FUNKTION
			for (int x = 0; x < actv.length; x++) {
				WeightInit weightinit = actv[x];

				/// SCHLEIFE VORERST ohne Funktion (algo = 0;)
				for (int f = 0; f < algo.length; f++) {
					
					trainData = new File(train_path);
					testData = new File(test_path);
					FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, RandNumGen);
					FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, RandNumGen);
					OptimizationAlgorithm optialgo = algo[f];
					ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
					ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);

					recordReader.initialize(train);
					recordReader.setListeners(new LogRecordListener());
					DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
					DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputnum);
					scaler.fit(dataIter);
					dataIter.setPreProcessor(scaler);

					// ParentPathLabelGenerator labelMaker2 = new ParentPathLabelGenerator();
					
					ImageRecordReader recordReader_Test = new ImageRecordReader(height, width, channels, labelMaker);
					recordReader_Test.initialize(test);
					
					
					// DataNormalization scaler2 = new ImagePreProcessingScaler(0, 1);
					DataSetIterator dataIter_test = new RecordReaderDataSetIterator(recordReader, batchSize, 1,
							outputnum);
					scaler.fit(dataIter_test);
					dataIter_test.setPreProcessor(scaler);

					if (networkType == NetworkType.OWN) {
						network = own(i, weightinit, optialgo);
						Nd4j.getMemoryManager().setAutoGcWindow(5000);
						// network = Kao();
						network.init();
					}

					if (networkType != NetworkType.GoogleNet) {

						List<IterationListener> listeners = new ArrayList<>();

						if (showinbrowser) {
							uiServer = UIServer.getInstance();
							statsStorage = new InMemoryStatsStorage();
							int listenerFrequency = 1;
							// network.setListeners(new StatsListener(statsStorage, listenerFrequency));
							uiServer.attach(statsStorage);
							listeners.add(new StatsListener(statsStorage, listenerFrequency));
							listeners.add(new ScoreIterationListener(10));
							network.setListeners(listeners);
						} else {
							network.setListeners(new ScoreIterationListener(10));
						}

					}
					// MultiLayerNetwork
					// MultiLayerNetwork network = lenetModel();
					// network.init();

					// EarlyStoppingModelSaver saver = new LocalFileModelSaver(path_model);

					for (int w = 0; w < epochscounter; w++) {
						while (dataIter.hasNext()) {
							// dataIter.next();
							// network.fit(dataIter);
							try {
								DataSet testSet = dataIter.next();

								// system.out.println(testSet);
								// System.out.println(testSet.getLabels());
								testSet.shuffle();
								// network.fit(testSet);
								if (networkType != NetworkType.GoogleNet) {
									network.fit(testSet);
								} else {
									googlenet.fit(testSet);
								}
								testSet = null;
								// System.out.println(testSet.getLabels().sum(0));

							} catch (Exception e) {
								LOGGER.info("FAILED to train Network! " + e.getMessage());
							}

							/*
							 * if(dataIter.hasNext() == false) { LOGGER.info("VALIDATING"); Evaluation eval
							 * = new Evaluation(2); recordReader.reset();
							 * 
							 * recordReader.initialize(test); DataSetIterator testIter = new
							 * RecordReaderDataSetIterator(recordReader,batchSize,1, outputnum);
							 * 
							 * //scaler.fit(testIter); //testIter.setPreProcessor(scaler);
							 * 
							 * while(testIter.hasNext()) { DataSet next = testIter.next(); next.shuffle();
							 * INDArray output = network.output(next.getFeatureMatrix());
							 * eval.eval(next.getLabels(), output);
							 * 
							 * System.out.println(next.getLabels().sum(0)); output = null; next = null; }
							 * System.out.println("ZWISCHENAKKURANZ: " + eval.accuracy());
							 * System.out.println("CONF MATRIX: " + eval.confusionToString());
							 * 
							 * testIter = null; eval = null;
							 * //System.out.println(network.getLabels().sum(0));
							 * 
							 * 
							 * LOGGER.info("EPOCHE Completed : " + i);
							 * 
							 * }
							 */

						}
						LOGGER.info("EPOCHE COMPLETED: " + w);
						LOGGER.info("EPOCHES Remaining: " + (epochscounter - w - 1));
						dataIter.reset();
					}

					/*
					 * EarlyStoppingConfiguration esConf = new EarlyStoppingConfiguration.Builder()
					 * .epochTerminationConditions(new MaxEpochsTerminationCondition(10)) //Max of
					 * 50 epochs .evaluateEveryNEpochs(1) .iterationTerminationConditions(new
					 * MaxTimeIterationTerminationCondition(20, TimeUnit.MINUTES))
					 * .iterationTerminationConditions(new
					 * InvalidScoreIterationTerminationCondition() )//Max of 20 minutes
					 * .scoreCalculator(new DataSetLossCalculator(dataIter_test, true)) //Calculate
					 * test set score .saveLastModel(false) //.modelSaver(saver) .build();
					 * 
					 * 
					 * // MultiLayerConfiguration configuration = own();
					 * 
					 * 
					 * // new EarlyStoppingTrainer() EarlyStoppingTrainer trainer = new
					 * EarlyStoppingTrainer(esConf,network,dataIter);
					 * 
					 * EarlyStoppingResult result = trainer.fit();
					 * 
					 * ////SCHREIBEN DER SACHEN
					 * 
					 * 
					 * 
					 * 
					 * 
					 * // DAS MUSS IN EINE TXT DATEI System.out.println("Termination reason: " +
					 * result.getTerminationReason()); System.out.println("Termination details: " +
					 * result.getTerminationDetails()); System.out.println("Total epochs: " +
					 * result.getTotalEpochs()); System.out.println("Best epoch number: " +
					 * result.getBestModelEpoch()); System.out.println("Score at best epoch: " +
					 * result.getBestModelScore());
					 * 
					 */

					// EVALUATION

					// Evaluation eval = new Evaluation(2);
					recordReader.reset();
					recordReader.initialize(test);
					DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputnum);
					scaler.fit(testIter);
					testIter.setPreProcessor(scaler);
					Evaluation eval = new Evaluation(2);

					if (networkType != NetworkType.GoogleNet) {
						eval = network.evaluate(testIter);
					} else {
						eval = googlenet.evaluate(testIter);
					}
					// scaler.fit(testIter);
					// testIter.setPreProcessor(scaler);

					// Evaluation eval = cg.evaluate(testIter);

					/*
					 * while(testIter.hasNext()) { DataSet next = testIter.next(); next.shuffle();
					 * //INDArray output = network.output(next.getFeatureMatrix()); INDArray output
					 * = cg.output(next.getFeatureMatrix())[0]; eval.eval(next.getLabels(), output);
					 * 
					 * 
					 * }
					 */

					if (eval.accuracy() > accuracy) {
						
						if (networkType == NetworkType.GoogleNet)
							bestNetwork = googlenet;
						
						else {
							bestNetwork = network;
						}
						accuracy = eval.accuracy();
						LOGGER.info("FOUND NEW BEST MODEL! ACCURACY: " + eval.accuracy());
						publish("FOUND NEW BEST MODEL! ACCURACY: " + eval.accuracy());

					}

					LOGGER.info(eval.stats());
					publish(eval.stats());
					LOGGER.info(Double.toString(eval.accuracy()));

					PrintWriter writer = null;
					;
					try {
						int zaehler = 1;
						File create = new File(output_path + "\\Textfiles");
						create.mkdirs();
						String txt_pfad = output_path + "\\Textfiles\\config" + zaehler + ".txt";
						while (new File(txt_pfad).exists()) {
							zaehler++;
							txt_pfad = output_path + "\\Textfiles\\config" + zaehler + ".txt";
						}

						writer = new PrintWriter(txt_pfad, "UTF-8");
						writer.println("Anzahl CNN LAYER :" + i);
						writer.println("BatchSize :" + batchSize);
						writer.println("Accurancy:" + eval.accuracy());
						writer.println("Confusion Matrix:" + eval.getConfusionMatrix());
						writer.println("------");
						writer.println("Updater: " + network.getUpdater().toString());
						writer.println("WeightInit:" + weightinit.toString());
						NeuralNetConfiguration netconf = network.conf();
						writer.println("LEARNING RATE POLICY:" + netconf.getLearningRatePolicy().toString());
						writer.println("SEED:" + seed);
						writer.println("OptimizationAlgo:" + optialgo.toString());
						writer.close();
						writer = null;

					} catch (Exception e) {
						if (writer != null) {
							writer.close();
						}

						LOGGER.info(e.getMessage());
					}


					try {
						// Model m = result.getBestModel();

						int counter = 0;
						String tmp_model_path = path_model + "\\modelconfig" + counter + ".zip";
						while (new File(tmp_model_path).exists()) {
							counter++;
							tmp_model_path = path_model + "\\modelconfig" + counter + ".zip";
						}

						if (networkType != NetworkType.GoogleNet) {
							ModelSerializer.writeModel(network, new File(tmp_model_path), true);
						} else {
							ModelSerializer.writeModel(googlenet, new File(tmp_model_path), true);
						}

						/// LABELS ZU DER ZIP DATEI HINZUFÜGEN

						addLabelsToZipFolder(labels, new File(tmp_model_path));

						/////

						/// HIER AUCH DAS GANZE NETZ ABSPEICHERN
					} catch (Exception e) {
						LOGGER.info("NOT SAVED : " + e.getMessage());
					}

					// network = null;
					
					recordReader.close();
					recordReader_Test.close();					
					recordReader = null;
					recordReader_Test = null;
					eval = null;
					dataIter = null;
					dataIter_test = null;
					scaler = null;
					labelMaker = null;
					optialgo = null;
					test = null;
					train = null;
					testData = null;
					trainData = null;


					/// NUR WEITER OPTIMIEREN WENN OWN
					if (networkType != NetworkType.OWN) {
						return null;
					}

				}
			}

		}

		if (bestNetwork != null) {
			int counter = 0;

			String tmp_model_path = path_model + "\\modelconfig_best" + counter + ".zip";
			while (new File(tmp_model_path).exists()) {
				counter++;
				tmp_model_path = path_model + "\\modelconfig_best" + counter + ".zip";
			}

			File bestModel = new File(tmp_model_path);
			bestModel.mkdirs();
			ModelSerializer.writeModel(bestNetwork, bestModel, true);
			LOGGER.info("MODEL STORED:" + tmp_model_path);

		}

		/*
		 * dataIter.next().shuffle(); for(int i = 0;i < epochscounter;i++) {
		 * while(dataIter.hasNext()) { //dataIter.next(); //network.fit(dataIter);
		 * DataSet testSet = dataIter.next(); testSet.shuffle(); network.fit(testSet);
		 * 
		 * // System.out.println(testSet.getLabels().sum(0));
		 * 
		 * 
		 * }
		 * 
		 * dataIter.reset(); LOGGER.info("EPOCHE Completed : " + i); }
		 * 
		 * recordReader.reset();
		 * 
		 * recordReader.initialize(test); DataSetIterator testIter = new
		 * RecordReaderDataSetIterator(recordReader,batchSize,1, outputnum);
		 * 
		 * scaler.fit(testIter); testIter.setPreProcessor(scaler); Evaluation eval = new
		 * Evaluation(2);
		 * 
		 * while(testIter.hasNext()) { DataSet next = testIter.next(); next.shuffle();
		 * INDArray output = network.output(next.getFeatureMatrix());
		 * eval.eval(next.getLabels(), output); }
		 * 
		 * LOGGER.info(eval.stats()); LOGGER.info(Double.toString(eval.accuracy()));
		 */

		// newTry(train_path, test_path, output_path, networkType);
	
		return null;
	}

	@Override
	protected void process(List<String> chunks) {

		for (String text : chunks) {

			try {
				JDP.append(text);
				jlabel.setText(Double.toString(accuracy));
			} catch (Exception e) {

				e.printStackTrace();
			}

		}

	}

	public int getCnn_min() {
		return cnn_min;
	}

	public void setCnn_min(int cnn_min) {
		this.cnn_min = cnn_min;
	}

	public int getCnn_max() {
		return cnn_max;
	}

	public void setCnn_max(int cnn_max) {
		this.cnn_max = cnn_max;
	}

	public int getEpochscounter() {
		return epochscounter;
	}

	public void setEpochscounter(int epochscounter) {
		this.epochscounter = epochscounter;
	}

}
