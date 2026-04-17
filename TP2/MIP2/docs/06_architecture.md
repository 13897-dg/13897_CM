# Architecture

**Architecture Pattern:** MVVM (Model-View-ViewModel)

**Layers:**
```text
UI (Activity/Fragment + XML) 
   ↓ observes
ViewModel (State Holder) 
   ↓ requests data
Repository (Data operations coordination)
   ↓ calls
API Service (Retrofit interface)
```

**Responsibilities:**
- **UI:** Render the Android views based on the states emitted by the ViewModel. Handle clicks and user inputs.
- **ViewModel:** Expose StateFlow or LiveData representing the UI state (Loading, Success, Error). Call the Repository to fetch data.
- **Repository:** Provide a clean API for ViewModel to get data. Hide the underlying data sources (in this phase, only the network. Later, local cache).
- **API Service:** Map network endpoints using Retrofit and Moshi/Gson.
