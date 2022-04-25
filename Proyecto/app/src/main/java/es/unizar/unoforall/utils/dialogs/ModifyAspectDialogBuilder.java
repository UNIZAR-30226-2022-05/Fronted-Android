package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.utils.TriConsumer;

public class ModifyAspectDialogBuilder {

    private final Activity activity;
    private final UsuarioVO usuarioVO;

    private final Integer puntos;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    private final View mainView;

    private final RadioGroup grupo1;
    private final RadioGroup grupo2;
    private final RadioGroup grupo3;

    public ModifyAspectDialogBuilder(Activity activity, UsuarioVO usuarioVO) {
        this.activity = activity;
        this.usuarioVO = usuarioVO;

        this.puntos = this.usuarioVO.getPuntos();

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.personalizacion_layout, null);

        this.grupo1 = this.mainView.findViewById(R.id.rgPer1);
        RadioButton child1 = (RadioButton) this.grupo1.getChildAt(this.usuarioVO.getAvatar());
        child1.setChecked(true);

        this.grupo2 = this.mainView.findViewById(R.id.rgPer2);
        RadioButton child2 = (RadioButton) this.grupo2.getChildAt(this.usuarioVO.getAspectoTablero());
        child2.setChecked(true);

        this.grupo3 = this.mainView.findViewById(R.id.rgPer3);
        RadioButton child3 = (RadioButton) this.grupo3.getChildAt(this.usuarioVO.getAspectoCartas());
        child3.setChecked(true);

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveButton(TriConsumer<Integer, Integer, Integer> consumer){
        this.positiveRunnable = () -> {

            switch (this.grupo1.getCheckedRadioButtonId()){
                case R.id.rbPerfil1:
                    if(this.puntos < 10){
                        Toast.makeText(activity, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        show();
                        return;
                    }
                    this.usuarioVO.setAvatar(1);
                case R.id.rbPerfil2:
                    if(this.puntos < 30){
                        Toast.makeText(activity, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        show();
                        return;
                    }
                    this.usuarioVO.setAvatar(2);
                case R.id.rbPerfil3:
                    if(this.puntos < 50){
                        Toast.makeText(activity, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        show();
                        return;
                    }
                    this.usuarioVO.setAvatar(3);
                case R.id.rbPerfil4:
                    if(this.puntos < 100){
                        Toast.makeText(activity, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        show();
                        return;
                    }
                    this.usuarioVO.setAvatar(4);
                case R.id.rbPerfil5:
                    if(this.puntos < 200){
                        Toast.makeText(activity, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        show();
                        return;
                    }
                    this.usuarioVO.setAvatar(5);
                case R.id.rbPerfil6:
                    if(this.puntos < 500){
                        Toast.makeText(activity, "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                        show();
                        return;
                    }
                    this.usuarioVO.setAvatar(6);
                default:
                    this.usuarioVO.setAvatar(0);
            }

            switch (this.grupo2.getCheckedRadioButtonId()){
                case R.id.rbFondo0:
                    this.usuarioVO.setAspectoTablero(0);
                case R.id.rbFondo1:
                    this.usuarioVO.setAspectoTablero(1);
                case R.id.rbFondo2:
                    this.usuarioVO.setAspectoTablero(2);
            }

            switch (this.grupo3.getCheckedRadioButtonId()){
                case R.id.rbCartas0:
                    this.usuarioVO.setAspectoCartas(0);
                case R.id.rbCartas1:
                    this.usuarioVO.setAspectoCartas(1);
            }

            consumer.accept(this.usuarioVO.getAvatar(), this.usuarioVO.getAspectoCartas(),
                    this.usuarioVO.getAspectoTablero());
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Personaliza tu cuenta");
        builder.setMessage("Cambia tu avatar, fondo o color de cartas");
        builder.setView(mainView);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }

}
