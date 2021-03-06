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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import javax.swing.JFileChooser;
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
import org.deeplearning4j.ui.stats.api.StatsReport;
import org.deeplearning4j.ui.storage.FileStatsStorage;
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

public class ThreadNet implements Runnable {
	private static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;

	protected static long seed = 42;
	protected static Random rng = new Random(seed);
	protected static double splitTrainTest = 0.8;
	private static int batchSize = 60;

	public static int getBatchSize() {
		return batchSize;
	}

	public static void setBatchSize(int batchSize) {
		ThreadNet.batchSize = batchSize;
	}

	private int epochscounter = 75;
	private int cnn_min = 1;
	private int cnn_max = 5;
	// private static final long seed = 12345;

	private static final Random randNumGen = new Random(seed);

	private static int height = 224;
	private static int width = 224;
	private static int channels = 3;
	private static int epochs = 50;
	private static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ThreadNet.class);
	private static String bestNetwork;
	private volatile double accuracy = 0;
	private volatile String tmp_model_path;
	private volatile String tmp_model_name;
	
	public String getTmp_model_name() {
		return tmp_model_name;
	}

	public void setTmp_model_name(String tmp_model_name) {
		this.tmp_model_name = tmp_model_name;
	}

	public String getTmp_model_path() {
		return tmp_model_path;
	}

	public void setTmp_model_path(String tmp_model_path) {
		this.tmp_model_path = tmp_model_path;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	private String train_path = "";
	private String test_path = "";
	private String output_path = "";
	private NetworkType networkType;
	private int amountoflayers;
	private int amountoffcc;
	private boolean maxp;
	private volatile String confusionmatrix;
	private boolean showinbrowser = true;
	private volatile String name = "";
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
File testmodel = null;
File testset = null;

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose Model");
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
		    testmodel = fileChooser.getSelectedFile();
		   // System.out.println("Selected file: " + selectedFile.getAbsolutePath());
		}
		
		 fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose Testset");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result2 = fileChooser.showOpenDialog(null);
		if (result2 == JFileChooser.APPROVE_OPTION) {
		   testset = fileChooser.getSelectedFile();
		 
		}
		Random RandNumGen = new Random(123);
		FileSplit test = new FileSplit(testset, NativeImageLoader.ALLOWED_FORMATS, RandNumGen);
		 MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(testmodel);
		
		Evaluation eval = new Evaluation(2);
		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		ImageRecordReader recordReader = new ImageRecordReader(224, 224, 3, labelMaker);
		recordReader.initialize(test);
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, 2);
		
		scaler.fit(dataIter);
		dataIter.setPreProcessor(scaler);
		
		
	DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, 2);
	scaler.fit(testIter);
		
	eval = restored.evaluate(testIter);
	
	System.out.print(eval.accuracy());
	System.out.print(eval.getConfusionMatrix());
	}

	public ThreadNet(String train, String test, String out, NetworkType nettype, boolean showinb, int amount, int batch,
			int epoch, int fc, boolean max) {
		// JDP = proggui.getTextArea();
		// jlabel = proggui.getLblWert();
		train_path = train;
		test_path = test;
		output_path = out;
		networkType = nettype;
		showinbrowser = showinb;
		amountoflayers = amount;
		batchSize = batch;
		epochscounter = epoch;
		maxp = max;
		amountoffcc = fc;
		name = getSaltString() + "-" + amountoflayers + "-" + amountoffcc;
	}

	public static MultiLayerNetwork Kao() {
		Map<Integer, Double> lrSchedule = new HashMap<>();
		lrSchedule.put(0, 0.1); // iteration #, learning rate
		lrSchedule.put(500, 0.01);
		lrSchedule.put(1000, 0.001);
		lrSchedule.put(1500, 0.0001);
		lrSchedule.put(2000, 0.00001);

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(1)
				.inferenceWorkspaceMode(WorkspaceMode.SINGLE).trainingWorkspaceMode(WorkspaceMode.SINGLE)
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

		// Nd4j.getMemoryManager().setAutoGcWindow(5000);

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

	public static HashMap<String, String> Try_model(File model, File[] images) throws Exception {
	//// LOAD MODEL
			Nd4j.getRandom().setSeed(12345);
			MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(model);
			HashMap<String, String> files = new HashMap<String, String>();
			List<String> labels = getLabelsFromModelZip(model);
			String[] classes = labels.toArray(new String[labels.size()]);
			
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

				
				DataNormalization scaler = new ImagePreProcessingScaler(0,1);
	            scaler.transform(image);
	            
				int[] predict = network.predict(image);
				String modelResult = "";//"Prediction: ";

				modelResult = modelResult + classes[predict[0]];

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
					//modelResult = output.toString();
					//modelResult += "===" + Arrays.toString(predict);
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

	
	private static MultiLayerNetwork newown(int amount_conv_layer, boolean maxpool, int amount_fcc)
	{
		double nonZeroBias = 0;// 1;
		double dropOut = 0.5;

		// ZU probierende Learningrates
		
		  Map<Integer, Double> lrSchedule = new HashMap<>(); lrSchedule.put(1, 0.1);
		  lrSchedule.put(100,0.05);
		  lrSchedule.put(200, 0.01); lrSchedule.put(500, 0.001); lrSchedule.put(1000,
		  0.0005); lrSchedule.put(2000, 0.0001);
		 
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		builder.seed(seed);
		// builder.weightInit(weight);
		builder.weightInit(WeightInit.RELU);
		builder.activation(Activation.RELU);
		// builder.setConvolutionMode(ConvolutionMode.Same);
		// builder.setMiniBatch(miniBatch);
		//builder.setUseRegularization(true);
		builder.regularization(true).l2(1e-5);
		// builder.convolutionMode(ConvolutionMode.Same);
		// if(weight == WeightInit.DISTRIBUTION)
		// {
		// builder.dist(new NormalDistribution(0.0, 0.01));
		// }
		builder.inferenceWorkspaceMode(WorkspaceMode.SEPARATE);
		builder.trainingWorkspaceMode(WorkspaceMode.SEPARATE);
		builder.iterations(1);
		builder.learningRate(0.001);
		//builder.learningRateDecayPolicy(LearningRatePolicy.Score);
		//builder.lrPolicyDecayRate(0.1);
//		builder.learningRateDecayPolicy(LearningRatePolicy.Step); //
		// builder.lrPolicyDecayRate(0.1).lrPolicySteps(10000);
		builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
		builder.updater(new Nesterovs(0.9));
	/*	builder.updater(Nesterovs.builder()
                .learningRate(1e-3)
                .momentum(0.9)
                .learningRateSchedule(new StepSchedule(
                        ScheduleType.EPOCH,
                        1e-2,
                        0.1,
                        20));*/
		//builder.learningRateDecayPolicy(LearningRatePolicy.)
		builder.learningRateDecayPolicy(LearningRatePolicy.Schedule);
		builder.learningRateSchedule(lrSchedule);
		// builder.updater(Updater.NESTEROVS);
		//builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
		//builder.updater(new Nesterovs(0.9));
			builder.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer);
		// builder.l2(1e-3);
		// builder.dropOut(0.5);

		// if(algo != null)
		// builder.optimizationAlgo(algo);
		//
		//builder.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer);
		//builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
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
		 //.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		 * //.learningRate(0.01) .learningRateDecayPolicy(LearningRatePolicy.Schedule)
		 * .learningRateSchedule(lrSchedule) // .biasLearningRate(1e-2*2) //
		 * .learningRateDecayPolicy(LearningRatePolicy.Step)
		 * //.learningRateDecayPolicy(LearningRatePolicy.Step) //
		 * .lrPolicyDecayRate(0.1) // .lrPolicySteps(100000) //.regularization(true) //
		 * .l2(5 * 1e-4) .list();
		 */
		listbuilder.layer(0,
				convInit("cnn1", channels, 64, new int[] { 11, 11 }, new int[] { 1, 1 }, new int[] { 0, 0 }, 0));
		listbuilder.layer(1, new LocalResponseNormalization.Builder().name("lrn1").build());
		listbuilder.layer(2, maxPool("maxpool1", new int[] { 2, 2 }));
		listbuilder.layer(3, conv5x5("cnn" + 2, 64, new int[] { 2, 2 }, new int[] { 0, 0 }, nonZeroBias));
		listbuilder.layer(4, new LocalResponseNormalization.Builder().name("lrn1").build());
		listbuilder.layer(5, maxPool("maxpool2", new int[] { 2, 2 }));
		int counter = 0;
		int cnncounter = 6;
		for (int i = 1; i <= amount_conv_layer; i++) {

			// listbuilder.layer(i+2, conv3x3("cnn"+i+2, 256, new int[] {1,1}, new int[]
			// {2,2}, nonZeroBias));
			listbuilder.layer(cnncounter, conv3x3("cnn" + i + 2, 64, nonZeroBias));
			cnncounter++;
			//listbuilder.layer(cnncounter, maxPool("maxpool1"+cnncounter, new int[] { 2, 2 }));
			//cnncounter++;
			// listbuilder.layer(i+1, conv5x5("cnn"+i+2,100,new int[] {5,5},new int[]
			// {0,0},nonZeroBias));
			// cnncounter = cnncounter+4;
			// counter = i+4;
		}
		// cnncounter++;
		

			listbuilder.layer(cnncounter, new LocalResponseNormalization.Builder().name("lrn2" + cnncounter).build());
			cnncounter++;
			listbuilder.layer(cnncounter, maxPool("maxpool" + cnncounter, new int[] { 2, 2 }));
			cnncounter++;
		
		
		counter = cnncounter;
		listbuilder.layer(counter,
				fullyConnected("ffn" + counter, 1000, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)));
		counter++;
		listbuilder.layer(counter,fullyConnected("ffn" + counter + 1, 256, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)));
		counter++;
		
		if(amount_fcc > 0)
		{
			listbuilder.layer(counter,fullyConnected("ffn" + counter + 1, 128, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)));
			counter++;
		}
		listbuilder.layer(counter,
				new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).name("output").nOut(2)
						// .nIn(256)
						.activation(Activation.SOFTMAX).build());
		listbuilder.backprop(true);
		listbuilder.pretrain(false);
		listbuilder.setInputType(InputType.convolutional(height, width, channels));
		MultiLayerConfiguration conf_test = listbuilder.build();

		return new MultiLayerNetwork(conf_test);
		
		
		
		
	}
	
	private static MultiLayerNetwork own(int amount_conv_layer) {
		double nonZeroBias = 0;// 1;
		double dropOut = 0.5;

		// ZU probierende Learningrates
		/*
		 * Map<Integer, Double> lrSchedule = new HashMap<>(); lrSchedule.put(1, 0.05);
		 * lrSchedule.put(200, 0.01); lrSchedule.put(300, 0.001); lrSchedule.put(500,
		 * 0.0001); lrSchedule.put(600, 0.00001);
		 */
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		builder.seed(seed);
		// builder.weightInit(weight);
		builder.weightInit(WeightInit.RELU);
		builder.activation(Activation.RELU);
		// builder.setConvolutionMode(ConvolutionMode.Same);
		// builder.setMiniBatch(miniBatch);
		// builder.setUseRegularization(true);
		builder.regularization(false).l2(0.0005);
		// builder.convolutionMode(ConvolutionMode.Same);
		// if(weight == WeightInit.DISTRIBUTION)
		// {
		// builder.dist(new NormalDistribution(0.0, 0.01));
		// }
		builder.inferenceWorkspaceMode(WorkspaceMode.SEPARATE);
		builder.trainingWorkspaceMode(WorkspaceMode.SEPARATE);
		builder.iterations(1);
		builder.learningRate(0.01);
		// builder.learningRateDecayPolicy(LearningRatePolicy.Schedule);
		// builder.learningRateSchedule(lrSchedule);
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
		// cnncounter++;
		listbuilder.layer(cnncounter, new LocalResponseNormalization.Builder().name("lrn2" + cnncounter).build());
		cnncounter++;
		listbuilder.layer(cnncounter, maxPool("maxpool" + cnncounter, new int[] { 2, 2 }));
		cnncounter++;
		counter = cnncounter;
		listbuilder.layer(counter,fullyConnected("ffn" + counter, 500, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)));
		// listbuilder.layer(counter + 1,
		// fullyConnected("ffn" + counter + 1, 256, nonZeroBias, dropOut, new
		// GaussianDistribution(0, 0.005)));

		listbuilder.layer(counter + 1,
				new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).name("output").nOut(2)
						// .nIn(256)
						.activation(Activation.SOFTMAX).build());
		listbuilder.backprop(true);
		listbuilder.pretrain(false);
		listbuilder.setInputType(InputType.convolutional(height, width, channels));
		MultiLayerConfiguration conf_test = listbuilder.build();

		return new MultiLayerNetwork(conf_test);

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
	public void run() {
LOGGER.info("AMOUNT OF LAYERS - BEGINNING : " + amountoflayers);
		int rngseed = 123;
		int outputnum = 2;
		Random RandNumGen = new Random(rngseed);
		LocalDateTime start = LocalDateTime.now();
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
							try {
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
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}

		}
		java.util.Collections.sort(labels);

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
		} else if (networkType == NetworkType.Kao) {
			network = Kao();
		}
		File dirFile = new File(output_path);

		dirFile.mkdir();

		trainData = new File(train_path);
		testData = new File(test_path);
		FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, RandNumGen);
		FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, RandNumGen);
		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);

		try {
			recordReader.initialize(train);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		recordReader.setListeners(new LogRecordListener());
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputnum);
		scaler.fit(dataIter);
		dataIter.setPreProcessor(scaler);

		// ParentPathLabelGenerator labelMaker2 = new ParentPathLabelGenerator();

		ImageRecordReader recordReader_Test = new ImageRecordReader(height, width, channels, labelMaker);
		try {
			recordReader_Test.initialize(test);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// DataNormalization scaler2 = new ImagePreProcessingScaler(0, 1);
		DataSetIterator dataIter_test = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputnum);
		scaler.fit(dataIter_test);
		dataIter_test.setPreProcessor(scaler);

		if (networkType == NetworkType.OWN) {
			network = null;
			LOGGER.info("BUILD MODEL with Layers: " + amountoflayers );
			//network = own(amountoflayers);
			network = newown(amountoflayers,maxp,amountoffcc);
			// Nd4j.getMemoryManager().setAutoGcWindow(5000);
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
				int tmp_counter = 0;
				
				tmp_model_path = path_model + "\\" + name +"_modelconfig" + tmp_counter + ".dl4j";
				while (new File(tmp_model_path).exists()) {
					tmp_counter++;
					tmp_model_path = path_model + "\\" + name + "_modelconfig" + tmp_counter + ".dl4j";
				}
				
				File statsFile = new File(tmp_model_path);
		        StatsStorage filestorage = new FileStatsStorage(statsFile);
		        listeners.add(new StatsListener(filestorage));
				network.setListeners(listeners);
			} else {
				int tmp_counter = 0;
				tmp_model_path = path_model + "\\" + name +"_modelconfig" + tmp_counter + ".dl4j";
				while (new File(tmp_model_path).exists()) {
					tmp_counter++;
					tmp_model_path = path_model + "\\" + name + "_modelconfig" + tmp_counter + ".dl4j";
				}
				
				
				
					File statsFile = new File(tmp_model_path);
		           StatsStorage filestorage = new FileStatsStorage(statsFile);
		            network.setListeners(new StatsListener(filestorage), new ScoreIterationListener(10));
				
				//network.setListeners(new ScoreIterationListener(10));
			}

		}
		
		
		
		// MultiLayerNetwork
		// MultiLayerNetwork network = lenetModel();
		// network.init();

		// EarlyStoppingModelSaver saver = new LocalFileModelSaver(path_model);

		for (int w = 0; w < epochscounter; w++) {
			int counter = dataIter.getLabels().size();
			
			LOGGER.info("DATAITER hat Einträge: " + counter);
			
			while (dataIter.hasNext()) {
				// dataIter.next();
				// network.fit(dataIter);
				
				try {
					DataSet testSet = dataIter.next();
					//LOGGER.info("TESTSET HAT EINTRÄGE: " + testSet.getLabels().sum(0));
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
			}
			LOGGER.info("EPOCHSCOUNTER:" + w + " Verbleibend:" + (epochscounter - (w +1)));
			dataIter.reset();
		}
		
		recordReader.reset();
		try {
			recordReader.initialize(test);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputnum);
		scaler.fit(testIter);
		testIter.setPreProcessor(scaler);
		Evaluation eval = new Evaluation(2);

		if (networkType != NetworkType.GoogleNet) {
			eval = network.evaluate(testIter);
		} else {
			eval = googlenet.evaluate(testIter);
		}

		LOGGER.info(eval.stats());

		LOGGER.info(Double.toString(eval.accuracy()));

		PrintWriter writer = null;
		;
		try {
			int zaehler = 1;
			File create = new File(output_path + "\\Textfiles");
			create.mkdirs();
			String txt_pfad = output_path + "\\Textfiles\\" + name + "_config" + zaehler + ".txt";
			while (new File(txt_pfad).exists()) {
				zaehler++;
				txt_pfad = output_path + "\\Textfiles\\"+ name + "_config" + zaehler + ".txt";
			}
			if (eval.accuracy() > accuracy) {

				bestNetwork = txt_pfad;

				this.setAccuracy( eval.accuracy());
				this.setConfusionmatrix(eval.getConfusionMatrix().toString());
				LOGGER.info("FOUND NEW BEST MODEL! ACCURACY: " + eval.accuracy());

			}
			writer = new PrintWriter(txt_pfad, "UTF-8");
			// writer.println("Anzahl CNN LAYER :" + i);
			writer.println("BatchSize :" + batchSize);
			writer.println("Epochen:" + epochscounter);
			writer.println("Accurancy:" + eval.accuracy());
			//writer.println("Learning Rate:" + network.getLayerWiseConfigurations().getl)
			writer.println("Confusion Matrix:" + eval.getConfusionMatrix());
			writer.println("EXCLUDED 1 Class from Predicition ? : " + (1== eval.averageF1NumClassesExcluded()));
			writer.println("ADDITIONAL CNN:" + amountoflayers);
			writer.println("ADDITIONAL FCC:" + amountoffcc);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			writer.println("START TIME: " + start.format(formatter));
			writer.println("END TIME:" + LocalDateTime.now().format(formatter));
			writer.println("");
			writer.println("------");
			writer.println("Updater: " + network.getUpdater().toString());
			// writer.println("WeightInit:" + weightinit.toString());
			NeuralNetConfiguration netconf = network.conf();
			writer.println("LEARNING RATE POLICY:" + netconf.getLearningRatePolicy().toString());
			writer.println("SEED:" + seed);
			writer.println("Trainings-Datensatz:"+ train_path);
			writer.println("Test-Datensatz:"+ test_path);
			// writer.println("OptimizationAlgo:" + optialgo.toString());
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
			 tmp_model_path = path_model + "\\" + name + "_modelconfig" + counter + ".zip";
			while (new File(tmp_model_path).exists()) {
				counter++;
				tmp_model_path = path_model + "\\" + name + "_modelconfig" + counter + ".zip";
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

		try {
			recordReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			recordReader_Test.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recordReader = null;
		recordReader_Test = null;
		eval = null;
		dataIter = null;
		dataIter_test = null;
		scaler = null;
		labelMaker = null;
		// optialgo = null;
		test = null;
		train = null;
		testData = null;
		trainData = null;
		network = null;
		/// NUR WEITER OPTIMIEREN WENN OWN

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

	public String getConfusionmatrix() {
		return confusionmatrix;
	}

	public void setConfusionmatrix(String confusionmatrix) {
		this.confusionmatrix = confusionmatrix;
	}

	protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

}
