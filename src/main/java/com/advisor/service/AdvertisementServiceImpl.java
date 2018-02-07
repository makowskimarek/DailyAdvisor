package com.advisor.service;

import com.advisor.model.entity.Advertisement;
import com.advisor.model.entity.User;
import com.advisor.model.request.AdvertisementRequest;
import com.advisor.model.response.AdvertisementResponse;
import com.advisor.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("AdvertisementService")
public class AdvertisementServiceImpl implements AdvertisementService {

    @Autowired
    @Qualifier("advertisementRepository")
    private AdvertisementRepository advertisementRepository;

    @Override
    public void setAdvertisement(User user, AdvertisementRequest advertisementRequest) {
        Advertisement advertisement = new Advertisement(user, advertisementRequest.getAdvText());
        advertisementRepository.save(advertisement);
    }


    @Override
    public List<AdvertisementResponse> selectAll() {
        List<AdvertisementResponse> advertisementResponseList = new ArrayList<AdvertisementResponse>();
        for (Advertisement advertisement : advertisementRepository.findAll()) {
            advertisementResponseList.add(new AdvertisementResponse(advertisement));
        }
        return advertisementResponseList;
    }

    @Override
    public AdvertisementResponse getAdvertisementByUser(User user) {
        Advertisement advertisement = advertisementRepository.findByUser(user);
        if (advertisement != null){
            return new AdvertisementResponse(advertisement);
        }
        else return null;
    }

    @Override
    public void updateAdvertisement(User user, AdvertisementRequest advertisementRequest) {
        advertisementRepository.updateAdvertisement(advertisementRequest.getAdvText(), user);
    }

}