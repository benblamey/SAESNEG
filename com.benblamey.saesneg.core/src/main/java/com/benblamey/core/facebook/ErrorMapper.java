package com.benblamey.core.facebook;

import com.restfb.DefaultJsonMapper.JsonMappingErrorHandler;
import com.restfb.types.Venue;

/**
 * A @see com.restfb.DefaultJsonMapper.JsonMappingErrorHandler for the RestFB
 * library, to work around an issue relating to Event venues.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class ErrorMapper implements JsonMappingErrorHandler {

    @Override
    public boolean handleMappingError(String arg0, Class<?> arg1, Exception arg2) {

        // {"description":"Hey all\nI am hosting a partylight morning on sundy 15th december at 11.00am (random time i know sorry but the lady doesnt do evenings on a sunday and i can only host on a weekend) \nI went to one of these parties last night and they sell a lovely range of candles, tea lights, candle holders, candles warmers and melts and also as its xmas a range of winter goods.  The things they sell are gorgeous! \nI would love it if you could all come and think you would all really like the products. \nIt will last no longer than two hours and for those with children if you cannot get childcare bring them along, with the obvious warning that there will be candles lit! I have a spare room where the children can play...and even keep my little kittie company lol! Also feel free to bring a friend, the more the merrier! The lady hosting it asks for payment on the night, you can pay with cash or debit card. \nIt wont let me add date and location so i do apologise but its ay my house!\nIf you need\/want any more info please feel free to text!\nPlease let me know if you can make it and again would love it if you could come\nLove Emma xx","is_date_only":false,"name":"Candle partylight party","owner":{"name":"Emma Cheetham","id":"545645098"},"privacy":"FRIENDS","start_time":"2013-12-15T11:00:00+0000","updated_time":"2013-11-24T12:12:19+0000","venue":[],"id":"571820712887053"}
        if (arg0.equals("[]") && arg1.equals(Venue.class) && arg2 == null) {
            // Known issue with venues of Events.
            return true;
        }

        return false;
    }

}
