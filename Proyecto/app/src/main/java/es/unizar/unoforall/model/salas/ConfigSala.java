package es.unizar.unoforall.model.salas;


public class ConfigSala {
	public enum ModoJuego{Original, Attack, Parejas, Undefined};
	
	private ModoJuego modoJuego;
	private ReglasEspeciales reglas;
	private int maxParticipantes;
	private boolean esPublica;
	
	public ConfigSala(ModoJuego modoJuego, ReglasEspeciales reglas, 
			int maxParticipantes, boolean esPublica) {
		super();
		this.modoJuego = modoJuego;
		this.reglas = reglas;
		this.maxParticipantes = maxParticipantes;
		this.esPublica = esPublica;
	}

	public ConfigSala() {}

	public void setModoJuego(ModoJuego modoJuego) {
		this.modoJuego = modoJuego;
	}

	public void setReglas(ReglasEspeciales reglas) {
		this.reglas = reglas;
	}

	public void setMaxParticipantes(int maxParticipantes) {
		this.maxParticipantes = maxParticipantes;
	}

	public void setEsPublica(boolean esPublica) {
		this.esPublica = esPublica;
	}

	public ModoJuego getModoJuego() {
		return modoJuego;
	}

	public ReglasEspeciales getReglas() {
		return reglas;
	}

	public int getMaxParticipantes() {
		return maxParticipantes;
	}

	public boolean isEsPublica() {
		return esPublica;
	}

	@Override
	public String toString() {
		return "ConfigSala [modoJuego=" + modoJuego + ", reglas=" + reglas + ", maxParticipantes=" + maxParticipantes
				+ ", esPublica=" + esPublica + "]";
	}
	
	
}
