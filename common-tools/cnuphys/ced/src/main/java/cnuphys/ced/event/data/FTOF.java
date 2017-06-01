package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

/**
 * static methods to centralize getting data arrays
 * 
 * @author heddle
 *
 */
public class FTOF extends DetectorData {

	// ftof constants
	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };

	
	//tdc adc hit list
	TdcAdcHitList _tdcAdcHits = new TdcAdcHitList("FTOF::tdc", "FTOF::adc");
	
	private static FTOF _instance;
	
	
	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static FTOF getInstance() {
		if (_instance == null) {
			_instance = new FTOF();
		}
		return _instance;
	}
	

	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_tdcAdcHits =  new TdcAdcHitList("FTOF::tdc", "FTOF::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the update l;ist
	 */
	public TdcAdcHitList updateTdcAdcList() {
		_tdcAdcHits =  new TdcAdcHitList("FTOF::tdc", "FTOF::adc");
		return _tdcAdcHits;
	}

	
	/**
	 * Get the avgX array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgX array
	 */
	public double[] avgX(int panelType) {
		return null;
	}
	
	
	/**
	 * Get the avgY array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgY array
	 */
	public double[] avgY(int panelType) {
		return null;
	}
	
	
	/**
	 * Get the avgZ array from the true data
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the avgZ array
	 */
	public double[] avgZ(int panelType) {
		return null;
	}

	/**
	 * Get the name from the panel type
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the name of the panel type
	 */
	public static String name(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		} else {
			return panelNames[panelType];
		}
	}

	/**
	 * Get the tdc and adc hit list
	 * @return the tdc adc hit list
	 */
	public TdcAdcHitList getHits() {
		return _tdcAdcHits;
	}
}
