package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "10_16");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: Wizard companion, strength 7, vitality 4; a fellowship ability only
					put("radagast", "31_17");   // Radagast, The Brown: a [gandalf] Wizard who is an ally, vitality 4; a fellowship ability only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard, vitality 4; stronger only beside 3 Ents
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion of twilight cost 2, strength 6, damage +1

					put("one1", "1_174");       // Goblin Backstabber: [moria] Orc, cost 1, strength 5; damage +2 only beside another [moria] Orc in its skirmish
					put("two1", "1_184");       // Goblin Wallcrawler: [moria] Orc, cost 2, strength 6, vitality 1; +2 only underground
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, strength 5; "Damage +1." is its whole text
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, strength 7, vitality 2; its text answers a weapon played on him
					put("three1", "1_179");     // Goblin Scavengers: [moria] Orc, cost 3, strength 8; its text is about being played
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, strength 9; "Damage +1." is its whole text

					// Saruman, Keeper of Isengard: the Shadow player's Wizard, cost 4, vitality 4. He may not be assigned to a
					// skirmish, and makes Uruk-hai fierce.
					put("saruman", "3_68");
					// The Mouth of Sauron, Bearer of Dread News: [wraith] Man, cost 3, strength 9, vitality 3.
					// "Response: If a Free Peoples event is played, exert this minion to cancel that event."
					put("mouth", "103_70");

					put("lemenya", "1_232");    // Úlairë Lemenya: Nazgûl, cost 4, strength 9; her text is about [wraith] conditions on companions
					put("nertea", "1_234");     // Úlairë Nertëa: Nazgûl, cost 4, strength 9; her trigger on being played needs 5 companions
					put("fell", "15_182");      // A Shadow Fell Over Them: "Assignment: Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatheringWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gathering Wind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert your Wizard to choose a number. Make each minion with that twilight cost strength -2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Gathering Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsYourWizardAndMakesEachMinionOfTheTwilightCostChosenStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(wind);
		scn.MoveCompanionsToTable(gandalf, avenger, gimli);
		scn.MoveAlliesToTable(radagast);

		var one1 = scn.GetShadowCard("one1");
		var two1 = scn.GetShadowCard("two1");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var three1 = scn.GetShadowCard("three1");
		var party = scn.GetShadowCard("party");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(one1, two1, savage, chieftain, three1, party, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetStrength(one1));
		assertEquals(6, scn.GetStrength(two1));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(8, scn.GetStrength(three1));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(wind));
		var pool = scn.GetTwilight();

		scn.FreepsPlayCard(wind);

		// A Wizard of yours, companion or ally: not the [gandalf] companion who is no Wizard, and not the Shadow player's Saruman.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, gimli, saruman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// Any number at all may be named.
		assertEquals(Integer.MAX_VALUE, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// The three minions of twilight cost 2, of three cultures, are 2 weaker. Every one of them is roaming here,
		// which is no part of a twilight cost: the Raiding Party, at 4, is what 2 and a roaming penalty would come to.
		assertEquals(4, scn.GetStrength(two1));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(one1));
		assertEquals(8, scn.GetStrength(three1));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(8, scn.GetStrength(saruman));
		// Gimli costs 2 as well, and is no minion.
		assertEquals(6, scn.GetStrength(gimli));

		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(wind);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileYourOnlyWizardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCardsToHand(wind);
		scn.MoveCompanionsToTable(gandalf, avenger);

		var saruman = scn.GetShadowCard("saruman");
		var three1 = scn.GetShadowCard("three1");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, three1);
		// Vitality 4 with 3 wounds: Gandalf cannot exert. The Ent, of his culture, and the Shadow player's Wizard could.
		scn.AddWoundsToChar(gandalf, 3);

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInHand(wind);
		assertFalse(scn.FreepsPlayAvailable(wind));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheMinionsStayWeakerThroughTheSkirmishesAndAreThemselvesAgainOnceTheRegroupPhaseBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(wind);
		scn.MoveCompanionsToTable(gandalf, gimli);

		var two1 = scn.GetShadowCard("two1");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(two1, chieftain);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		scn.FreepsChoose("2");
		assertEquals(4, scn.GetStrength(two1));
		assertEquals(5, scn.GetStrength(chieftain));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, two1},
				new PhysicalCardImpl[]{gandalf, chieftain});

		// Gimli, 6, would lose to the Wallcrawler's printed 6, the Shadow player winning a tie. Against 4 he wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(two1);
		assertEquals(0, scn.GetWoundsOn(gimli));

		// In the second skirmish of the turn the Chieftain is still at 7 - 2.
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(chieftain));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(chieftain));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain));
	}

	@Test
	public void AMinionOfThatCostPlayedAfterwardsKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(wind);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);

		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var fell = scn.GetShadowCard("fell");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToDiscard(nertea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		scn.FreepsChooseCard(radagast);
		// The ally pays.
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("4");
		assertEquals(7, scn.GetStrength(lemenya));

		// "Assignment: Spot a Nazgûl and remove (4) to play a Nazgûl from your discard pile."
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertInDiscard(nertea);
		scn.ShadowUseCardAction(fell);

		// Nertëa costs 4 as well, and was not in play when the event was played.
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(4, nertea.getBlueprint().getTwilightCost());
		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(7, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheNumberIsChosenAsPartOfTheCostBeforeTheShadowPlayerMayCancelTheEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(wind);
		scn.MoveCompanionsToTable(gandalf);

		var mouth = scn.GetShadowCard("mouth");
		var three1 = scn.GetShadowCard("three1");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, three1);

		scn.SkipToPhase(Phase.MANEUVER);
		var pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(wind);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Exerting the Wizard and choosing the number are both the cost: the number is named before the Shadow
		// player is given the chance to answer the event.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertEquals(Integer.MAX_VALUE, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");

		// "Response: If a Free Peoples event is played, exert this minion to cancel that event."
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));

		// Cancelled: neither minion of twilight cost 3 is weaker, and what was paid stays paid.
		assertEquals(9, scn.GetStrength(mouth));
		assertEquals(8, scn.GetStrength(three1));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(wind);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
