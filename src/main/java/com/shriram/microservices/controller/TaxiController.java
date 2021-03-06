package com.shriram.microservices.controller;

import com.shriram.microservices.model.customer.Customer;
import com.shriram.microservices.model.location.Location;
import com.shriram.microservices.model.taxi.Taxi;
import com.shriram.microservices.service.TaxiService;
import com.shriram.microservices.util.ResponseUtil;
import com.shriram.microservices.valueObject.TaxiValueObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Taxi Controller for search and booking a cab
 * <p>
 * This controller searches and books a cab for a given location and a customer
 *
 * @method - searchTaxis - retrieves all nearby for a given location
 * @method - bookTaxi - books the closest taxi for a given location
 */
@RestController
@RequestMapping("/taxi")
public class TaxiController {

    @Autowired
    @Qualifier("taxiService")
    private TaxiService taxiService;

    Logger logger = Logger.getLogger(TaxiController.class);

    /**
     * Searches the nearest taxis available for a given location
     * <p>
     * This method searches the nearest taxi available near a given location
     * <p>
     * url - /taxi/search
     *
     * @param request   - HttpServletRequest
     * @param response  - HttpServletResponse
     * @param latitude  - Latitude of the given location
     * @param longitude - Longitude of the given location
     */
    @RequestMapping(method = RequestMethod.GET, value = "/search")
    @ResponseBody
    public Object searchTaxis(@RequestParam("latitude") String latitude,
                              @RequestParam("longitude") String longitude, HttpServletRequest request,
                              HttpServletResponse response) {
        if (StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) {
            return ResponseUtil.badRequest(response, "missing latitude/longitude");
        }

        Location location = new Location(Double.parseDouble(latitude), Double.parseDouble(longitude));
        List<Taxi> retrieveTaxis = taxiService.searchTaxis(location);
        List<TaxiValueObject> taxis = new ArrayList<>();

        for (Taxi taxi : retrieveTaxis) {
            TaxiValueObject taxiValueObject = new TaxiValueObject(taxi);
            taxis.add(taxiValueObject);
        }

        return taxis;
    }

    /**
     * Books the nearest taxi available for a given location
     * <p>
     * This method books the nearest taxi available near a given location, or returns null for no taxi available
     * <p>
     * url - /taxi/book
     *
     * @param request    - HttpServletRequest
     * @param response   - HttpServletResponse
     * @param customerID - Customer ID of the customer
     * @param latitude   - Latitude of the given location
     * @param longitude  - Longitude of the given location
     */
    @RequestMapping(method = RequestMethod.POST, value = "/book")
    @ResponseBody
    public Map<String, Object> bookTaxi(@RequestParam("customerID") String customerID, @RequestParam("latitude") String latitude,
                                        @RequestParam("longitude") String longitude, @RequestParam("isPink") String isPink, HttpServletRequest request,
                                        HttpServletResponse response) {
        Map<String, Object> responseContent = new HashMap<>();
        if (StringUtils.isEmpty(customerID) || StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) {
            return ResponseUtil.badRequest(response, "missing customer id/latitude/longitude");
        }

        Location location = new Location(Double.parseDouble(latitude), Double.parseDouble(longitude));

        Taxi retrievedTaxi = taxiService.searchTaxi(location, Boolean.getBoolean(isPink));
        Customer retrievedCustomer = taxiService.bookTaxi(retrievedTaxi, new Customer(customerID));

        responseContent.put("message", "booked");
        responseContent.put("taxi", new TaxiValueObject(retrievedCustomer.taxi()));
        return responseContent;
    }

}
