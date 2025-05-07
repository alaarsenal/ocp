package ca.qc.hydro.epd.service.wsclient.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HorodateurOutputBeanWrapper<T extends HorodateurOutputBean> {

    private List<T> valeurs = new ArrayList<>();

}
