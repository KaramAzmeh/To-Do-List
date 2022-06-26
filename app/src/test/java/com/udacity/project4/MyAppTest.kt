package com.udacity.project4

/*
class MyAppTest: Application() {


    override fun onCreate() {
        super.onCreate()

        Log.i("MyAppTest", "MyApp Called")
        */
/**
         * use Koin Library as a service locator
         *//*

        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as FakeDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as FakeDataSource
                )
            }
//            single { RemindersLocalRepository(get()) as FakeDataSource }
            single { LocalDB.createRemindersDao(this@MyAppTest) }
        }

        startKoin {
            androidContext(this@MyAppTest)
            modules(listOf(myModule))
        }

    }

}*/
