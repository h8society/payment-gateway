import {BrowserRouter, Route, Routes} from "react-router-dom";
import PayPage from "./pages/PayPage.tsx";

function App() {

  return (
      <BrowserRouter>
        <Routes>
          <Route path="/pay/:transactionId" element={<PayPage />} />
        </Routes>
      </BrowserRouter>
  )
}

export default App
