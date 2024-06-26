package com.generalbytes.batm.server.extensions.extra.stellar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generalbytes.batm.common.currencies.CryptoCurrency;
import com.generalbytes.batm.server.extensions.AbstractExtension;
import com.generalbytes.batm.server.extensions.ExtensionsUtil;
import com.generalbytes.batm.server.extensions.ICryptoAddressValidator;
import com.generalbytes.batm.server.extensions.IWallet;
import com.generalbytes.batm.server.extensions.extra.stellar.wallets.stellar.StellarCoinWallet;
import com.generalbytes.batm.server.extensions.extra.stellar.wallets.stellar.consts.Const;
import com.generalbytes.batm.server.extensions.extra.stellar.wallets.stellar.dto.Wallet;
import com.google.gson.Gson;

public class StellarCoinExtension extends AbstractExtension {
	private static final Logger log = LoggerFactory.getLogger(StellarCoinExtension.class);
	String requestBody;

	@Override
	public String getName() {
		return "BATM Stellarcoin extension";
	}

	@Override
	public IWallet createWallet(String walletLogin, String tunnelPassword) {
		
		log.debug("Stellar:CreateWallet Started");
		if (walletLogin != null && !walletLogin.trim().isEmpty()) {
			try {
				log.debug("Stellar: Step 1");
				StringTokenizer st = new StringTokenizer(walletLogin, ":");
				String walletType = st.nextToken();

				if ("bpventures.us".equalsIgnoreCase(walletType)) {
					log.debug("Stellar: Step 2");
					String secret = st.nextToken();
					String apikey = st.nextToken();
					String hostname = st.nextToken();
					String testnet = st.nextToken();
					if (testnet.equals("true")) {
						log.debug("Stellar: Step 3");
						requestBody = "{\"testnet\":"+ testnet+" , \"secret\":"+secret+"}";
					}
					try {
						log.debug("Stellar: Step 4");
						URL url = new URL(hostname + Const.ADDWALLET);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setRequestProperty("Authorization", "Token " + apikey);
						connection.setDoOutput(true);
						try (OutputStream os = connection.getOutputStream()) {
							byte[] input = requestBody.getBytes("utf-8");
							os.write(input, 0, input.length);
						}
						try (BufferedReader reader = new BufferedReader(
								new InputStreamReader(connection.getInputStream()))) {
							String line;
							while ((line = reader.readLine()) != null) {
								Gson gson = new Gson();
								Wallet wallet = gson.fromJson(reader.readLine(), Wallet.class);
								wallet.setApiKey(apikey);
								wallet.setSecret(secret);
								log.debug("Stellar: Step 5");
								return new StellarCoinWallet(wallet);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						connection.disconnect();

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			} catch (Exception e) {
				ExtensionsUtil.logExtensionParamsException("createWallet", getClass().getSimpleName(), walletLogin, e);
			}
		}
		return null;

	}

	@Override
	public ICryptoAddressValidator createAddressValidator(String cryptoCurrency) {
		log.debug("Stellar: Step 6");
		if (CryptoCurrency.XLM.getCode().equalsIgnoreCase(cryptoCurrency)) {
			return new StellarcoinAddressValidator();
		}
		return null;
	}
	
	@Override
	    public Set<String> getSupportedCryptoCurrencies() {
		log.debug("Stellar: Step 7");
	        Set<String> result = new HashSet<String>();
	        result.add(CryptoCurrency.XLM.getCode());
	        return result;
	    }

}
