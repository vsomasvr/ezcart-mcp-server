/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.ezcart.mcp.server;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

import java.net.http.HttpRequest;
import java.util.Map;

public class EzcartMcpClient {

	private final McpClientTransport transport;

	public static void main(String[] args) {
		// 1. Obtain a bearer token from your OAuth2 provider (e.g., using client credentials grant).
		// This is a placeholder. Replace with your actual token retrieval logic.
		String bearerToken = "your-jwt-token-here";

		McpClientTransport clientTransport = HttpClientSseClientTransport.builder("http://localhost:9090/api/v1/mcp")
				.requestBuilder(HttpRequest.newBuilder()
						.header("Authorization", "Bearer " + bearerToken).header("Content-Type", "application/json"))
				.build();
	    new EzcartMcpClient(clientTransport).run();
	}

	public EzcartMcpClient(McpClientTransport transport) {
		this.transport = transport;
	}

	public void run() {

		var client = McpClient.sync(this.transport).build();

		client.initialize();

		client.ping();

		// List and demonstrate tools
		ListToolsResult toolsList = client.listTools();
		System.out.println("Available Tools = " + toolsList);
		toolsList.tools().stream().forEach(tool -> {
			System.out.println("Tool: " + tool.name() + ", description: " + tool.description() + ", schema: " + tool.inputSchema());
		});
		System.out.println("\n");

//		CallToolResult getProductsByCategoryResult = client.callTool(new CallToolRequest("getProductsByCategory", Map.of("category", "LAPTOP")));
//		System.out.println("Products by Category: " + getProductsByCategoryResult);
//		System.out.println("\n");

		CallToolResult searchProductsResult = client.callTool(new CallToolRequest("searchProducts", Map.of("query", "professional laptop", "categories", new String[]{"LAPTOP"}, "minPrice", 500.0, "maxPrice", 3000.0, "manufacturers", new String[]{""} ,"ramFilters", new String[]{"32GB", "16GB"}, "storageFilters", new String[]{"512GB"}, "processorFilters", new String[]{})));
		System.out.println("Search Results: " + searchProductsResult);
		System.out.println("\n");

		CallToolResult getProductByIdResult = client.callTool(new CallToolRequest("getProductById", Map.of("productId", "AETHER-CORE-15")));
		System.out.println("Product by Id: " + getProductByIdResult);
		System.out.println("\n");

		CallToolResult getProductReviewsResult = client.callTool(new CallToolRequest("getProductReviews", Map.of("productId", "AETHER-CORE-15")));
		System.out.println("Product Reviews: " + getProductReviewsResult);
		System.out.println("\n");

		CallToolResult getUserReviewsResult = client.callTool(new CallToolRequest("getUserReviews", Map.of("userId", "creativeFlow")));
		System.out.println("User Reviews: " + getUserReviewsResult);

		System.out.println("\n--- Testing Cart --- ");

		CallToolResult addToCartResult = client.callTool(new CallToolRequest("addToCart", Map.of("productId", "AETHER-CORE-15", "quantity", 1)));
		System.out.println("Add to cart: " + addToCartResult);

		CallToolResult getCartResult = client.callTool(new CallToolRequest("getCart", Map.of()));
		System.out.println("Get cart: " + getCartResult);

		CallToolResult updateCartItemResult = client.callTool(new CallToolRequest("updateCartItem", Map.of("productId", "AETHER-CORE-15", "quantity", 2)));
		System.out.println("Update cart item: " + updateCartItemResult);

		CallToolResult removeFromCartResult = client.callTool(new CallToolRequest("removeFromCart", Map.of("productId", "AETHER-CORE-15")));
		System.out.println("Remove from cart: " + removeFromCartResult);

		CallToolResult clearCartResult = client.callTool(new CallToolRequest("clearCart", Map.of()));
		System.out.println("Clear cart: " + clearCartResult);

		client.closeGracefully();

	}

}
