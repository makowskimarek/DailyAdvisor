package com.advisor.controllers;

import com.advisor.model.entity.Advertisement;
import com.advisor.model.entity.User;
import com.advisor.model.entity.UserProfile;
import com.advisor.model.request.AdvCriteriaRequest;
import com.advisor.model.request.AdvertisementRequest;
import com.advisor.model.response.AdvertisementResponse;
import com.advisor.service.AdvertisementService;
import com.advisor.service.Exceptions.AdvertisementNotFound;
import com.advisor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AdvertisementController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdvertisementService advertisementService;

    @RequestMapping(value = { "advertisement/add" }, method = RequestMethod.POST)
    public ResponseEntity addAdvertisement(@RequestBody AdvertisementRequest advertisementRequest)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        if (advertisementService.getAdvertisementByUser(user) == null){
            advertisementService.setAdvertisement(user, advertisementRequest);

            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.IM_USED);
        }
    }

    @RequestMapping(value = { "advertisement/getAll" }, method = RequestMethod.GET)
    public ResponseEntity<List<AdvertisementResponse>> getAllAdvertisement()
    {
        List<AdvertisementResponse> advertisementList = advertisementService.selectAll();
        if (advertisementList.size() != 0){
            return new ResponseEntity<>(advertisementList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = { "advertisement/get/{userId}" }, method = RequestMethod.GET)
    public ResponseEntity<AdvertisementResponse> getAdvertisementByUserId(@PathVariable Long userId)
    {
        User user = userService.findUserById(userId);

        if(user != null){
            Advertisement advertisement = advertisementService.getActiveAdvertisementByUser(user);
            if(advertisement != null){
                if(advertisement.getUser().equals(user)){
                    advertisementService.addVisit(advertisement);
                }
                return new ResponseEntity<>(new AdvertisementResponse(advertisement), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = { "/advertisement/update" }, method = RequestMethod.PUT)
    public ResponseEntity updateAdvertisement(@RequestBody AdvertisementRequest advertisementRequest)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());

        advertisementService.updateAdvertisement(user, advertisementRequest);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = { "/advertisement/active/{advId}" }, method = RequestMethod.PUT)
    public ResponseEntity activeAdvertisement(@PathVariable long advId)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
    try {
        advertisementService.updateStatus(advId, user, "active");
    } catch(AdvertisementNotFound e){
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = { "/advertisement/disable/{advId}" }, method = RequestMethod.PUT)
    public ResponseEntity disableAdvertisement(@PathVariable long advId)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        try {
            advertisementService.updateStatus(advId, user, "disabled");
        } catch(AdvertisementNotFound e){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = { "advertisement/getByCriteria" }, method = RequestMethod.POST)
    public ResponseEntity<List<AdvertisementResponse>> getByCriteria(@RequestBody AdvCriteriaRequest advCriteriaRequest)
    {
        List<UserProfile> userProfiles = userService.findByCity(advCriteriaRequest.getCity());
        List<User> users = new ArrayList<>();
        userProfiles.forEach(userProfile->users.add(userProfile.getUser()));
        List<Advertisement> advertisementList = advertisementService.getByCriteria(users, advCriteriaRequest.getCoachType());
        List<AdvertisementResponse> advertisementResponses = new ArrayList<>();
        advertisementList.forEach(advertisement->advertisementResponses.add(new AdvertisementResponse(advertisement)));
        if (advertisementList.size() != 0){
            return new ResponseEntity<>(advertisementResponses, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
