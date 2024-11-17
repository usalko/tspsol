/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import SwiftUI
import shared
import Foundation

struct ContentView: View {
    var mainViewModel: MainViewModel

    // The ViewModel exposes a StateFlow.
    // We collect() the StateFlow into State, which can be used in SwiftUI.
    // https://skie.touchlab.co/features/flows-in-swiftui
    @State
    var homeUIState: HomeUiState = HomeUiState(tspsol: [])

    var body: some View {
        Text("Tspsols").font(.largeTitle).fontWeight(.bold)
        CartView(mainViewModel: mainViewModel)
        ScrollView {
            LazyVStack {
                ForEach(homeUIState.tspsol, id: \.self) { value in
                    TspsolView(tspsol: value, addToCart: { tspsol in
                        Task {
                            self.mainViewModel.addItemToCart(tspsol: tspsol)
                        }
                    })
                }
            }
            // https://skie.touchlab.co/features/flows-in-swiftui
            .collect(flow: self.mainViewModel.homeUiState, into: $homeUIState)
        }
    }
}

struct TspsolView: View {
    var tspsol: Tspsol
    var addToCart: (Tspsol) -> Void
    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            ZStack {
                RoundedRectangle(cornerRadius: 15).fill(Color(red: 0.8, green: 0.8, blue: 1.0))
                VStack {
                    Text("\(tspsol.name)")
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text("\(tspsol.fullName)")
                        .frame(maxWidth: .infinity, alignment: .leading)
                }.padding()
                Spacer()
                Button(action: { addToCart(tspsol) }, label: {
                    Text("Add")
                }).padding().frame(maxWidth: .infinity, alignment: .trailing)
            }.padding([.leading, .trailing])
        }
    }
}
