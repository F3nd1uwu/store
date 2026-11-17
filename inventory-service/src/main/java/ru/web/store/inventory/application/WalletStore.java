package ru.web.store.inventory.application;

import ru.web.store.inventory.domain.Wallet;

public interface WalletStore {
  Wallet deposit(Wallet amount);

  Wallet get(java.util.UUID user);
}
