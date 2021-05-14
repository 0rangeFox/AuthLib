package com.mojang.authlib.yggdrasil;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YggdrasilGameProfileRepository
        implements GameProfileRepository {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASE_URL = "https://api.mojang.com/";
    private static final String SEARCH_PAGE_URL = "https://api.mojang.com/profiles/";
    private static final int ENTRIES_PER_PAGE = 2;
    private static final int MAX_FAIL_COUNT = 3;
    private static final int DELAY_BETWEEN_PAGES = 100;
    private static final int DELAY_BETWEEN_FAILURES = 750;
    private final YggdrasilAuthenticationService authenticationService;

    public YggdrasilGameProfileRepository(YggdrasilAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void findProfilesByNames(String[] names, Agent agent, ProfileLookupCallback callback) {
        HashSet<String> criteria = Sets.newHashSet();
        for (String name : names) {
            if (Strings.isNullOrEmpty((String)name)) continue;
            criteria.add(name.toLowerCase());
        }
        int page = 0;
        for (List<String> request : Iterables.partition(criteria, (int)2)) {
            boolean failed;
            int failCount = 0;
            do {
                failed = false;
                try {
                    ProfileSearchResultsResponse response = (ProfileSearchResultsResponse)((Object)this.authenticationService.makeRequest(HttpAuthenticationService.constantURL((String)(SEARCH_PAGE_URL + agent.getName().toLowerCase())), request, ProfileSearchResultsResponse.class));
                    failCount = 0;
                    LOGGER.debug("Page {} returned {} results, parsing", new Object[]{page, response.getProfiles().length});
                    HashSet<String> missing = Sets.newHashSet((Iterable)request);
                    for (GameProfile profile : response.getProfiles()) {
                        LOGGER.debug("Successfully looked up profile {}", new Object[]{profile});
                        missing.remove(profile.getName().toLowerCase());
                        callback.onProfileLookupSucceeded(profile);
                    }
                    for (String name : missing) {
                        LOGGER.debug("Couldn't find profile {}", new Object[]{name});
                        callback.onProfileLookupFailed(new GameProfile(null, name), (Exception)((Object)new ProfileNotFoundException("Server did not find the requested profile")));
                    }
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException ignored) {}
                }
                catch (AuthenticationException e) {
                    if (++failCount == 3) {
                        for (String name : request) {
                            LOGGER.debug("Couldn't find profile {} because of a server error", new Object[]{name});
                            callback.onProfileLookupFailed(new GameProfile(null, name), (Exception)((Object)e));
                        }
                        continue;
                    }
                    try {
                        Thread.sleep(750L);
                    }
                    catch (InterruptedException ignored) {
                        // empty catch block
                    }
                    failed = true;
                }
            } while (failed);
        }
    }
}
