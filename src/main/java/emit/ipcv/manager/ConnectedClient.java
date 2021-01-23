/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emit.ipcv.manager;

import com.sun.corba.se.spi.orb.Operation;
import emit.ipcv.dataFormat.DataPacket;
import emit.ipcv.engines.interfaces.Operations;
import emit.ipcv.engines.localProcessing.linear.GaussianFilter;
import emit.ipcv.engines.localProcessing.linear.MiddleFilter;
import emit.ipcv.engines.localProcessing.linear.discreteCovolution.*;
import emit.ipcv.engines.localProcessing.nonLinear.AverageFilter;
import emit.ipcv.engines.localProcessing.nonLinear.ConservativeFilter;
import emit.ipcv.engines.localProcessing.nonLinear.MedianFilter;
import emit.ipcv.engines.operations.AllOrNothing;
import emit.ipcv.engines.operations.Dilation;
import emit.ipcv.engines.operations.Erosion;
import emit.ipcv.engines.pointProcessing.DynamicDisplay;
import emit.ipcv.engines.pointProcessing.HistogramEqualization;
import emit.ipcv.engines.pointProcessing.InvertGrayscale;
import emit.ipcv.engines.pointProcessing.ThresholdingBinarization;
import emit.ipcv.engines.transformation2D.*;
import emit.ipcv.utils.Const;
import emit.ipcv.utils.colors.RGBA;
import emit.ipcv.utils.imageHelper.GrayscaleImageHelper;
import emit.ipcv.utils.imageHelper.RgbImageHelper;
import emit.ipcv.utils.thresholding.Otsu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rinelfi
 */
public class ConnectedClient implements Runnable {
	
	private final Socket socket;
	
	public ConnectedClient(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(this.socket.getInputStream());
			DataPacket dataPacket = (DataPacket) objectInputStream.readObject();
			System.out.printf("[INFO] New request from %s:%d\n\tRequest header : \"%s\"\n", socket.getInetAddress().getHostAddress(), socket.getPort(), dataPacket.getHeader());
			switch (dataPacket.getHeader()) {
				case Const.PING:
					send(dataPacket);
					break;
				case Const.SHEAR:
					shearOperation(dataPacket);
					break;
				case Const.HOMOTHETY:
					homothetyOperation(dataPacket);
					break;
				case Const.ROTATION:
					rotationOperation(dataPacket);
					break;
				case Const.VERTICAL_SYMMETRY:
					verticalSymmetryOperation(dataPacket);
					break;
				case Const.HORIZONTAL_SYMMETRY:
					horizontalSymmetryOperation(dataPacket);
					break;
				case Const.SYMMETRY_IN_THE_CENTER:
					symmetryInTheCenterOperation(dataPacket);
					break;
				case Const.COLOR_INVERSION:
					colorInversionOperation(dataPacket);
					break;
				case Const.BINARIZATION:
					binarizationOperation(dataPacket);
					break;
				case Const.HISTOGRAM_EQUALIZATION:
					histogramEqualizationOperation(dataPacket);
					break;
				case Const.DYNAMIC_DISPLAY:
					dynamicDisplayOperation(dataPacket);
					break;
				case Const.SOBEL_FILTER1:
					sobelFilter1Operation(dataPacket);
					break;
				case Const.SOBEL_FILTER2:
					sobelFilter2Operation(dataPacket);
					break;
				case Const.LAPLACIAN_FILTER:
					laplacianFilterOperation(dataPacket);
					break;
				case Const.REHAUSSEUR_FILTER:
					rehausseurFilterOperation(dataPacket);
					break;
				case Const.PERSONAL_FILTER1:
					personalFilter1Operation(dataPacket);
					break;
				case Const.PERSONAL_FILTER2:
					personalFilter2Operation(dataPacket);
					break;
				case Const.PERSONAL_FILTER3:
					personalFilter3Operation(dataPacket);
					break;
				case Const.MEDIUM_FILTERING:
					mediumFilteringOperation(dataPacket);
					break;
				case Const.GAUSSIAN_FILTERING:
					gaussianFilteringOperation(dataPacket);
					break;
				case Const.CONSERVATIVE_SMOOTHING:
					conservativeSmoothing(dataPacket);
					break;
				case Const.MEDIAN_FILTERING:
					medianFilteringOperation(dataPacket);
					break;
				case Const.AVERAGE_FILTERING:
					averageFilterOperatin(dataPacket);
					break;
				case Const.EROSION:
					erosionOperation(dataPacket);
					break;
				case Const.DILATION:
					dilationOperation(dataPacket);
					break;
				case Const.OPENING:
					openingOperation(dataPacket);
					break;
				case Const.ALL_OR_NOTHING:
					allOrNothingOperation(dataPacket);
					break;
				case Const.THICKENING:
					thickeningOperation(dataPacket);
					break;
				case Const.TOP_HAT_OPENING:
					topHatOpeningOperation(dataPacket);
					break;
				case Const.TOP_HAT_CLOSURE:
					topHatClosureOperation(dataPacket);
					break;
				case Const.PAPERT_TURTLE:
					papertTurtleOperation(dataPacket);
					break;
			}
		} catch (IOException | ClassNotFoundException ex) {
			Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				if (objectInputStream != null) {
					objectInputStream.close();
				}
			} catch (IOException ex) {
				System.out.println("[WARNING] Client is disconnected");
				Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private void papertTurtleOperation(DataPacket dataPacket) throws IOException {
	
	}
	
	private void topHatClosureOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		int x = (int) inputData.get("x");
		int y = (int) inputData.get("y");
		int[][] structuringElement = (int[][]) inputData.get("structuring-element");
		RGBA[][] image = (RGBA[][]) inputData.get("image");
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] binaryGrayscale = new ThresholdingBinarization(rgbImageHelper.getGrayscale(), new Otsu(rgbImageHelper.getGrayscale()).execute()).execute();
		int[][] closingGrayscale = new Erosion(structuringElement, x, y, new Dilation(structuringElement, x, y, binaryGrayscale).execute()).execute();
		GrayscaleImageHelper openingGrayscaleHelper = new GrayscaleImageHelper(closingGrayscale);
		int[][] newGrayscale = new int[openingGrayscaleHelper.lineLength()][openingGrayscaleHelper.columnLength()];
		
		for (int line = 0; line < openingGrayscaleHelper.lineLength(); line++) {
			for (int column = 0; column < openingGrayscaleHelper.columnLength(); column++) {
				newGrayscale[line][column] = Math.abs(binaryGrayscale[line][column] - closingGrayscale[line][column]);
			}
		}
		newGrayscale = new InvertGrayscale(newGrayscale).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(newGrayscale).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.TOP_HAT_OPENING).setData(fullColorImage));
	}
	
	private void topHatOpeningOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		int x = (int) inputData.get("x");
		int y = (int) inputData.get("y");
		int[][] structuringElement = (int[][]) inputData.get("structuring-element");
		RGBA[][] image = (RGBA[][]) inputData.get("image");
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] binaryGrayscale = new ThresholdingBinarization(rgbImageHelper.getGrayscale(), new Otsu(rgbImageHelper.getGrayscale()).execute()).execute();
		int[][] openingGrayscale = new Dilation(structuringElement, x, y, new Erosion(structuringElement, x, y, binaryGrayscale).execute()).execute();
		GrayscaleImageHelper openingGrayscaleHelper = new GrayscaleImageHelper(openingGrayscale);
		int[][] newGrayscale = new int[openingGrayscaleHelper.lineLength()][openingGrayscaleHelper.columnLength()];
		
		for (int line = 0; line < openingGrayscaleHelper.lineLength(); line++) {
			for (int column = 0; column < openingGrayscaleHelper.columnLength(); column++) {
				newGrayscale[line][column] = Math.abs(binaryGrayscale[line][column] - openingGrayscale[line][column]);
			}
		}
		newGrayscale = new InvertGrayscale(newGrayscale).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(newGrayscale).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.TOP_HAT_OPENING).setData(fullColorImage));
	}
	
	private void thickeningOperation(DataPacket dataPacket) throws IOException {
	
	}
	
	private void allOrNothingOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		int[][] structuringElement = (int[][]) inputData.get("structuring-element");
		RGBA[][] image = (RGBA[][]) inputData.get("image");
		Operations operation = new AllOrNothing(structuringElement, new RgbImageHelper(image).getGrayscale());
		int[][] allOrNothing = operation.execute();
		
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		RGBA[][] fullColorImage = new RgbImageHelper(allOrNothing).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.DILATION).setData(fullColorImage));
	}
	
	private void openingOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		int x = (int) inputData.get("x");
		int y = (int) inputData.get("y");
		int[][] structuringElement = (int[][]) inputData.get("structuring-element");
		RGBA[][] image = (RGBA[][]) inputData.get("image");
		
		Operations erosion = new Erosion(structuringElement, x, y, new RgbImageHelper(image).getGrayscale());
		int[][] erosionOperation = erosion.execute();
		Operations dilation = new Dilation(structuringElement, x, y, erosionOperation);
		int[][] newGrayscale = dilation.execute();
		
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		RGBA[][] fullColorImage = new RgbImageHelper(newGrayscale).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.DILATION).setData(fullColorImage));
	}
	
	private void dilationOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		int x = (int) inputData.get("x");
		int y = (int) inputData.get("y");
		int[][] structuringElement = (int[][]) inputData.get("structuring-element");
		RGBA[][] image = (RGBA[][]) inputData.get("image");
		Operations operation = new Dilation(structuringElement, x, y, new RgbImageHelper(image).getGrayscale());
		int[][] dilation = operation.execute();
		
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		RGBA[][] fullColorImage = new RgbImageHelper(dilation).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.DILATION).setData(fullColorImage));
	}
	
	private void erosionOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		int x = (int) inputData.get("x");
		int y = (int) inputData.get("y");
		int[][] structuringElement = (int[][]) inputData.get("structuring-element");
		RGBA[][] image = (RGBA[][]) inputData.get("image");
		Operations operation = new Erosion(structuringElement, x, y, new RgbImageHelper(image).getGrayscale());
		int[][] erosion = operation.execute();
		
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		RGBA[][] fullColorImage = new RgbImageHelper(erosion).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.EROSION).setData(fullColorImage));
	}
	
	private void averageFilterOperatin(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new AverageFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new AverageFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new AverageFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.AVERAGE_FILTERING).setData(fullColorImage));
	}
	
	private void medianFilteringOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new MedianFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new MedianFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new MedianFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.MEDIAN_FILTERING).setData(fullColorImage));
	}
	
	private void conservativeSmoothing(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new ConservativeFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new ConservativeFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new ConservativeFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.CONSERVATIVE_SMOOTHING).setData(fullColorImage));
	}
	
	private void gaussianFilteringOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new GaussianFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new GaussianFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new GaussianFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.GAUSSIAN_FILTERING).setData(fullColorImage));
	}
	
	private void mediumFilteringOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new MiddleFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new MiddleFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new MiddleFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.MEDIUM_FILTERING).setData(fullColorImage));
	}
	
	private void personalFilter3Operation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new CustomFilter3(rgbImageHelper.getReds()).execute(),
			greenOperation = new CustomFilter3(rgbImageHelper.getGreens()).execute(),
			blueOperation = new CustomFilter3(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.PERSONAL_FILTER3).setData(fullColorImage));
	}
	
	private void personalFilter2Operation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new CustomFilter2(rgbImageHelper.getReds()).execute(),
			greenOperation = new CustomFilter2(rgbImageHelper.getGreens()).execute(),
			blueOperation = new CustomFilter2(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.PERSONAL_FILTER2).setData(fullColorImage));
	}
	
	private void personalFilter1Operation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new CustomFilter1(rgbImageHelper.getReds()).execute(),
			greenOperation = new CustomFilter1(rgbImageHelper.getGreens()).execute(),
			blueOperation = new CustomFilter1(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.PERSONAL_FILTER1).setData(fullColorImage));
	}
	
	private void rehausseurFilterOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new RehausseurFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new RehausseurFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new RehausseurFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.REHAUSSEUR_FILTER).setData(fullColorImage));
	}
	
	private void laplacianFilterOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new LaplacianFilter(rgbImageHelper.getReds()).execute(),
			greenOperation = new LaplacianFilter(rgbImageHelper.getGreens()).execute(),
			blueOperation = new LaplacianFilter(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.LAPLACIAN_FILTER).setData(fullColorImage));
	}
	
	private void sobelFilter2Operation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new SobelFilter2(rgbImageHelper.getReds()).execute(),
			greenOperation = new SobelFilter2(rgbImageHelper.getGreens()).execute(),
			blueOperation = new SobelFilter2(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.SOBEL_FILTER2).setData(fullColorImage));
	}
	
	private void sobelFilter1Operation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new SobelFilter1(rgbImageHelper.getReds()).execute(),
			greenOperation = new SobelFilter1(rgbImageHelper.getGreens()).execute(),
			blueOperation = new SobelFilter1(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.SOBEL_FILTER1).setData(fullColorImage));
	}
	
	private void dynamicDisplayOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		final RGBA[][] image = (RGBA[][]) inputData.get("image");
		final int[] minMax = (int[]) inputData.get("min-max");
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new DynamicDisplay(rgbImageHelper.getReds(), minMax[0], minMax[1]).execute(),
			greenOperation = new DynamicDisplay(rgbImageHelper.getGreens(), minMax[0], minMax[1]).execute(),
			blueOperation = new DynamicDisplay(rgbImageHelper.getBlues(), minMax[0], minMax[1]).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.DYNAMIC_DISPLAY).setData(fullColorImage));
	}
	
	private void histogramEqualizationOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[] redHistogram = new HistogramEqualization(rgbImageHelper.getReds()).execute(),
			greenHistogram = new HistogramEqualization(rgbImageHelper.getGreens()).execute(),
			blueHistogram = new HistogramEqualization(rgbImageHelper.getBlues()).execute();
		
		final int lineLength = rgbImageHelper.lineLength(), columnLength = rgbImageHelper.columnLength();
		int[][] redOperation = new int[lineLength][columnLength], greenOperation = new int[lineLength][columnLength], blueOperation = new int[lineLength][columnLength];
		
		// red
		for (int line = 0; line < lineLength; line++) {
			for (int column = 0; column < columnLength; column++) {
				redOperation[line][column] = redHistogram[image[line][column].getRed()];
			}
		}
		
		// green
		for (int line = 0; line < lineLength; line++) {
			for (int column = 0; column < columnLength; column++) {
				greenOperation[line][column] = greenHistogram[image[line][column].getGreen()];
			}
		}
		
		// blue
		for (int line = 0; line < lineLength; line++) {
			for (int column = 0; column < columnLength; column++) {
				blueOperation[line][column] = blueHistogram[image[line][column].getBlue()];
			}
		}
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.HISTOGRAM_EQUALIZATION).setData(fullColorImage));
	}
	
	private void binarizationOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> inputData = (HashMap) dataPacket.getData();
		final RGBA[][] image = (RGBA[][]) inputData.get("image");
		final int[][] grayscale = new RgbImageHelper(image).getGrayscale();
		final RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		ThresholdingBinarization operation;
		if (inputData.containsKey("threshold")) operation = new ThresholdingBinarization(grayscale, (int) inputData.get("threshold"));
		else {
			Otsu otsu = new Otsu(grayscale);
			operation = new ThresholdingBinarization(grayscale, otsu.execute());
		}
		RGBA[][] fullColorImage = new RgbImageHelper(operation.execute()).setAlphas(rgbImageHelper.getAlphas()).getImage();
		send(new DataPacket().setHeader(Const.BINARIZATION).setData(fullColorImage));
	}
	
	private void colorInversionOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		
		int[][] redOperation = new InvertGrayscale(rgbImageHelper.getReds()).execute(),
			greenOperation = new InvertGrayscale(rgbImageHelper.getGreens()).execute(),
			blueOperation = new InvertGrayscale(rgbImageHelper.getBlues()).execute();
		
		RGBA[][] fullColorImage = new RgbImageHelper(rgbImageHelper.getAlphas(), redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.COLOR_INVERSION).setData(fullColorImage));
	}
	
	private void symmetryInTheCenterOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		int[][] alphaOperation = new SymmetryO2D(rgbImageHelper.getAlphas()).execute(),
			redOperation = new SymmetryO2D(rgbImageHelper.getReds()).execute(),
			greenOperation = new SymmetryO2D(rgbImageHelper.getGreens()).execute(),
			blueOperation = new SymmetryO2D(rgbImageHelper.getBlues()).execute();
		RGBA[][] fullColorImage = new RgbImageHelper(alphaOperation, redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.SYMMETRY_IN_THE_CENTER).setData(fullColorImage));
	}
	
	private void horizontalSymmetryOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		int[][] alphaOperation = new SymmetryY2D(rgbImageHelper.getAlphas()).execute(),
			redOperation = new SymmetryY2D(rgbImageHelper.getReds()).execute(),
			greenOperation = new SymmetryY2D(rgbImageHelper.getGreens()).execute(),
			blueOperation = new SymmetryY2D(rgbImageHelper.getBlues()).execute();
		RGBA[][] fullColorImage = new RgbImageHelper(alphaOperation, redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.HORIZONTAL_SYMMETRY).setData(fullColorImage));
	}
	
	private void verticalSymmetryOperation(DataPacket dataPacket) throws IOException {
		final RGBA[][] image = (RGBA[][]) dataPacket.getData();
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		int[][] alphaOperation = new SymmetryX2D(rgbImageHelper.getAlphas()).execute(),
			redOperation = new SymmetryX2D(rgbImageHelper.getReds()).execute(),
			greenOperation = new SymmetryX2D(rgbImageHelper.getGreens()).execute(),
			blueOperation = new SymmetryX2D(rgbImageHelper.getBlues()).execute();
		RGBA[][] fullColorImage = new RgbImageHelper(alphaOperation, redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.VERTICAL_SYMMETRY).setData(fullColorImage));
	}
	
	private void shearOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> data = (HashMap) dataPacket.getData();
		float[] constants = (float[]) data.get("constants");
		RGBA[][] image = (RGBA[][]) data.get("image");
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		int[][] alphaOperation = new Shear2D(rgbImageHelper.getAlphas(), constants).execute(),
			redOperation = new Shear2D(rgbImageHelper.getReds(), constants).execute(),
			greenOperation = new Shear2D(rgbImageHelper.getGreens(), constants).execute(),
			blueOperation = new Shear2D(rgbImageHelper.getBlues(), constants).execute();
		RGBA[][] fullColorImage = new RgbImageHelper(alphaOperation, redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.SHEAR).setData(fullColorImage));
	}
	
	private void homothetyOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> data = (HashMap) dataPacket.getData();
		float[] constants = (float[]) data.get("constants");
		RGBA[][] image = (RGBA[][]) data.get("image");
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		int[][] alphaOperation = new Homothety2D(rgbImageHelper.getAlphas(), constants).execute(),
			redOperation = new Homothety2D(rgbImageHelper.getReds(), constants).execute(),
			greenOperation = new Homothety2D(rgbImageHelper.getGreens(), constants).execute(),
			blueOperation = new Homothety2D(rgbImageHelper.getBlues(), constants).execute();
		RGBA[][] fullColorImage = new RgbImageHelper(alphaOperation, redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.HOMOTHETY).setData(fullColorImage));
	}
	
	private void rotationOperation(DataPacket dataPacket) throws IOException {
		final Map<String, Object> data = (HashMap) dataPacket.getData();
		float angle = (int) data.get("angle");
		RGBA[][] image = (RGBA[][]) data.get("image");
		RgbImageHelper rgbImageHelper = new RgbImageHelper(image);
		int[][] alphaOperation = new Rotation2D(rgbImageHelper.getAlphas(), angle).execute(),
			redOperation = new Rotation2D(rgbImageHelper.getReds(), angle).execute(),
			greenOperation = new Rotation2D(rgbImageHelper.getGreens(), angle).execute(),
			blueOperation = new Rotation2D(rgbImageHelper.getBlues(), angle).execute();
		RGBA[][] fullColorImage = new RgbImageHelper(alphaOperation, redOperation, greenOperation, blueOperation).getImage();
		send(new DataPacket().setHeader(Const.ROTATION).setData(fullColorImage));
	}
	
	private void send(DataPacket dataPacket) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
		objectOutputStream.writeObject(dataPacket);
		objectOutputStream.flush();
		objectOutputStream.close();
	}
	
}
