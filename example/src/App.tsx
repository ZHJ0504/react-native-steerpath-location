import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import {
  start,
  hasStarted,
  getUserLocation,
} from 'react-native-steerpath-location';

export default function App() {
  const [result, setResult] = React.useState<boolean | undefined>();

  React.useEffect(() => {
    setResult(hasStarted());
  }, []);

  React.useEffect(() => {
    start();
  }, []);
  setInterval(function () {
    //this code runs every 5 second
    if (!hasStarted()) {
      console.log('has not been started');
      start();
    }
  }, 5000);

  setInterval(function () {
    //this code runs every 5 second
    if (hasStarted()) {
      let location = getUserLocation();
      console.log('in get user location and has been started');
      console.log(location);
    } else {
      console.log('in get user location and has not been started');
    }
  }, 5000);
  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
