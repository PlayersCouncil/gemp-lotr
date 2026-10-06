package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizard", "103_40");
					put("wizard2", "103_40");

					put("saruman", "3_69");     // Saruman, Servant of the Eye: [isengard] Wizard minion, vitality 4
					put("rabble", "4_33");      // Saruman, Rabble-rouser: a Saruman who is a [dunland] minion
					put("follower", "17_38");   // Saruman, Servant of Sauron: a Saruman who is a follower
					put("white", "32_24");      // Saruman, The White: a Saruman who is a Free Peoples ally
					put("uruk1", "1_151");      // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("uruk2", "1_151");
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability never used here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text at all
					put("hollowing", "3_54");   // Hollowing of Isengard: [isengard] condition in the support area
					put("palantir", "3_67");    // The Palantir of Orthanc: [isengard] artifact in the support area
					put("armory", "1_173");     // Goblin Armory: [moria] condition in the support area
					put("journey", "1_253");    // Journey Into Danger: while you can spot 5 companions, each companion's twilight cost is +2

					put("galadriel", "13_15");  // Galadriel, Sorceress of the Hidden Land: the one unbound companion who costs 0
					put("merry", "1_302");      // Merry, Friend to Sam: costs 1
					put("pippin", "1_306");     // Pippin, Friend to Frodo: costs 1
					put("gimli", "1_13");       // Gimli, Son of Gloin: costs 2
					put("sam", "1_311");        // Sam, Son of Hamfast: costs 2, Ring-bound
					put("boromir", "1_97");     // Boromir, Son of Denethor: costs 3
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: 3 is printed on him; his own text makes it 2 while he can spot a [rohan] Man, himself included
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: costs 4
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally who costs 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWizardKnowsBetterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: A Wizard Knows Better
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Restore 2 [Isengard] cards.  Then you may exert X [isengard] minions (or spot Saruman and spot X [isengard] minions) to hinder an unbound companion costing up to X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wizard");

		assertEquals("A Wizard Knows Better", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresTwoOfThreeHinderedIsengardCardsForTwoAndTheHinderMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var troop = scn.GetShadowCard("troop");
		var hollowing = scn.GetShadowCard("hollowing");
		var palantir = scn.GetShadowCard("palantir");
		var armory = scn.GetShadowCard("armory");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToHand(wizard);
		scn.MoveCardsToSupportArea(hollowing, palantir, armory);
		scn.MoveCompanionsToTable(gimli, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Hindered: an [isengard] minion, condition and artifact; a [moria] minion and condition; a companion.
		// The other Uruk Savage is [isengard] and is not hindered.
		scn.HinderCard(uruk1, hollowing, palantir, troop, armory, gimli);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(wizard));
		assertInHand(wizard);

		scn.ShadowPlayCard(wizard);
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, hollowing, palantir));
		assertFalse(scn.ShadowHasCardChoiceAvailable(troop));
		assertFalse(scn.ShadowHasCardChoiceAvailable(armory));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(uruk2));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(uruk1, hollowing);
		assertFalse(scn.IsHindered(uruk1));
		assertFalse(scn.IsHindered(hollowing));
		assertTrue(scn.IsHindered(palantir));
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(gimli));

		// Both Uruk Savages could now exert and Merry costs 1, and the Shadow player says no.
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(uruk2));
		assertFalse(scn.IsHindered(merry));
		assertInDiscard(wizard);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneInThePoolNorInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.SetTwilight(1);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wizard));

		scn.ShadowPass();
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wizard));
	}

	@Test
	public void ExertsXIsengardMinionsToHinderAnUnboundCompanionCostingUpToX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var wizard2 = scn.GetShadowCard("wizard2");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		var silinde = scn.GetFreepsCard("silinde");

		scn.MoveCardsToHand(wizard, wizard2);
		scn.MoveCompanionsToTable(merry, pippin, gimli, sam, boromir, eomer);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2, servant, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(uruk1);
		scn.SetTwilight(4);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		// The one hindered [isengard] card is restored, and the rest of the event goes on with only one restored.
		assertFalse(scn.IsHindered(uruk1));

		scn.ShadowChooseYes();
		// Any [isengard] minion may exert, the one just restored among them; the [moria] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, uruk2, servant));
		assertFalse(scn.ShadowHasCardChoiceAvailable(troop));
		assertEquals(3, scn.ShadowGetChoiceMax());

		// X = 1
		scn.ShadowChooseCards(uruk2);
		assertEquals(1, scn.GetWoundsOn(uruk2));
		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(troop));

		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, pippin));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(silinde));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(merry);
		assertTrue(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(pippin));
		assertInDiscard(wizard);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		scn.ShadowPlayCard(wizard2);
		scn.ShadowChooseYes();
		// The Uruk Savage that exerted has vitality 3 and can exert again.
		assertEquals(3, scn.ShadowGetChoiceMax());

		// X = 2
		scn.ShadowChooseCards(uruk1, servant);
		assertEquals(1, scn.GetWoundsOn(uruk1));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetWoundsOn(uruk2));

		// Costing up to 2: Pippin (1) and Gimli (2). Not Boromir (3), nor Eomer, who has 3 printed on him
		// whatever his own text makes of it, nor Sam (2, but Ring-bound), nor the ally (2), nor Merry, already
		// hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pippin, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(boromir));
		assertFalse(scn.ShadowHasCardChoiceAvailable(eomer));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sam));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo));
		assertFalse(scn.ShadowHasCardChoiceAvailable(silinde));
		assertFalse(scn.ShadowHasCardChoiceAvailable(merry));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(pippin));
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSarumanOnTheTableMinionsAreSpottedAndNoneIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var saruman = scn.GetShadowCard("saruman");
		var uruk1 = scn.GetShadowCard("uruk1");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var hollowing = scn.GetShadowCard("hollowing");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(wizard);
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCompanionsToTable(merry, gimli, boromir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, uruk1, servant, troop);
		// The Isengard Servant is exhausted: it could not exert, and it can be spotted.
		scn.AddWoundsToChar(servant, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		scn.ShadowChooseYes();

		// The [isengard] minions, Saruman among them; not the [moria] Orc and not the [isengard] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, uruk1, servant));
		assertFalse(scn.ShadowHasCardChoiceAvailable(troop));
		assertFalse(scn.ShadowHasCardChoiceAvailable(hollowing));

		// X = 3
		scn.ShadowChooseCards(saruman, uruk1, servant);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(2, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(troop));

		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, gimli, boromir));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));

		scn.ShadowChooseCard(boromir);
		assertTrue(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASarumanRestoredByTheFirstSentenceIsThenSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var saruman = scn.GetShadowCard("saruman");
		var uruk1 = scn.GetShadowCard("uruk1");
		var hollowing = scn.GetShadowCard("hollowing");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToHand(wizard);
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCompanionsToTable(merry, gimli, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, uruk1);
		// Exhausted, so that exerting could not reach a companion who costs 2.
		scn.AddWoundsToChar(uruk1, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.HinderCard(saruman, hollowing);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		// Exactly two hindered [isengard] cards: both are restored without a choice.
		assertFalse(scn.IsHindered(saruman));
		assertFalse(scn.IsHindered(hollowing));

		scn.ShadowChooseYes();
		// X = 2: Saruman and the exhausted Uruk Savage
		scn.ShadowChooseCards(saruman, uruk1);
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(2, scn.GetWoundsOn(uruk1));

		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(boromir));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SarumanTheFollowerIsSarumanToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var follower = scn.GetShadowCard("follower");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(merry, gimli, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		// Both exhausted: neither could exert, and both can be spotted.
		scn.AddWoundsToChar(uruk1, 2);
		scn.AddWoundsToChar(uruk2, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Put down in the maneuver phase, after the follower's own offer at its start would have been made.
		scn.MoveCardsToSupportArea(follower);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		scn.ShadowChooseYes();

		// X = 2
		scn.ShadowChooseCards(uruk1, uruk2);
		assertEquals(2, scn.GetWoundsOn(uruk1));
		assertEquals(2, scn.GetWoundsOn(uruk2));

		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(boromir));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayersSarumanIsSarumanToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var white = scn.GetFreepsCard("white");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(merry, gimli, boromir);
		scn.MoveAlliesToTable(white);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		// Both exhausted: neither could exert, and both can be spotted.
		scn.AddWoundsToChar(uruk1, 2);
		scn.AddWoundsToChar(uruk2, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		scn.ShadowChooseYes();

		// X = 2
		scn.ShadowChooseCards(uruk1, uruk2);
		assertEquals(2, scn.GetWoundsOn(uruk1));
		assertEquals(2, scn.GetWoundsOn(uruk2));

		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(boromir));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASarumanOfAnotherCultureIsSpottedButNotCountedAndXOfZeroHindersTheCompanionWhoCostsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var rabble = scn.GetShadowCard("rabble");
		var troop = scn.GetShadowCard("troop");
		var galadriel = scn.GetFreepsCard("galadriel");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(galadriel, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(rabble, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		assertFalse(scn.IsHindered(galadriel));
		scn.ShadowChooseYes();

		// Saruman is there to spot, there is no [isengard] minion, and X is 0. Galadriel is the only unbound
		// companion in the game who costs 0, so there is nothing to choose between.
		assertTrue(scn.IsHindered(galadriel));
		assertFalse(scn.IsHindered(merry));
		assertEquals(0, scn.GetWoundsOn(rabble));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionWhoseCostAnotherCardHasRaisedIsCountedAtTheCostPrintedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var journey = scn.GetShadowCard("journey");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");

		scn.MoveCardsToHand(wizard, pippin);
		scn.MoveCardsToSupportArea(journey);
		// With Frodo, 5 companions
		scn.MoveCompanionsToTable(merry, gimli, boromir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);

		// Journey Into Danger is in force: Pippin, in hand, would cost 1 + 2 to play. Gimli, in play, costs
		// the 2 printed on him (F-0109).
		assertEquals(3, scn.GetTwilightCost(pippin));
		assertEquals(2, scn.GetTwilightCost(gimli));
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		scn.ShadowChooseYes();
		// X = 2
		scn.ShadowChooseCards(uruk1, uruk2);

		// In play each costs what is printed on him: Merry 1, Gimli 2, Boromir 3, Aragorn 4.
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, gimli));
		assertFalse(scn.ShadowHasCardChoiceAvailable(boromir));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutSarumanAndWithNoMinionAbleToExertTheCompanionWhoCostsNothingIsNotHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var galadriel = scn.GetFreepsCard("galadriel");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(galadriel, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1);
		// The only [isengard] minion is exhausted.
		scn.AddWoundsToChar(uruk1, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);

		// "Exert X minions" needs at least one minion to exert (Q-0076), so Galadriel, who costs 0, cannot be
		// hindered for nothing: the event is paid for and spent, and the Shadow player is asked nothing.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(wizard);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetWoundsOn(uruk1));
		assertFalse(scn.IsHindered(galadriel));
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheHinderIsNotOfferedWhenNoIsengardMinionCanExertAndSarumanIsNotThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var troop = scn.GetShadowCard("troop");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(merry, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, troop);
		// The only [isengard] minion is exhausted; the [moria] Orc could exert.
		scn.AddWoundsToChar(uruk1, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(wizard));
		scn.ShadowPlayCard(wizard);

		// The event is paid for and spent, and the Shadow player is asked nothing.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(wizard);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheHinderIsNotOfferedWhenSarumanIsThereAndNoCompanionCostsAsLittleAsTheIsengardMinionsToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		// Saruman is the only [isengard] minion, so X can be no more than 1; the [moria] Orc is not counted.
		scn.MoveMinionsToTable(saruman, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(wizard));
		scn.ShadowPlayCard(wizard);

		// Gimli costs 2 and Aragorn 4, and Frodo is Ring-bound: nobody could be hindered, and spotting is no
		// cost that might be worth paying for its own sake. The event is paid for and spent, and the Shadow
		// player is asked nothing.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(wizard);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutSarumanAtLeastOneMinionMustBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var galadriel = scn.GetFreepsCard("galadriel");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(galadriel, merry, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		scn.ShadowChooseYes();

		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, uruk2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		// X = 1
		scn.ShadowChooseCards(uruk1);
		assertEquals(1, scn.GetWoundsOn(uruk1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(galadriel, merry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gimli));

		scn.ShadowChooseCard(galadriel);
		assertTrue(scn.IsHindered(galadriel));
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
