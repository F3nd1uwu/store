package ru.web.store.auth.application;

import java.util.*;
import org.springframework.stereotype.Service;
import ru.web.store.auth.domain.CustomerProfile;
import ru.web.store.contracts.Json;

@Service
public class ProfileService {
  private final DocumentStore store;

  public ProfileService(DocumentStore store) {
    this.store = store;
  }

  public CustomerProfile save(CustomerProfile profile) {
    store.save("profile", profile.id(), profile.id(), Json.write(profile));
    return profile;
  }

  public Optional<CustomerProfile> get(UUID id) {
    return store.find("profile", id, id).map(s -> Json.read(s, CustomerProfile.class));
  }
}
