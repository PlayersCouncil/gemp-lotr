package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "6_77");
					put("balrog", "12_79");     // The Balrog, The Terror of Khazad-dum: [moria] Balrog, unique, cost 12, strength 17, site 4;
					                            // its text acts only at an underground site
					put("flame", "6_76");       // The Balrog, Terror of Flame and Shadow: [moria] Balrog, unique, cost 14; -3 to play
					                            // while a [moria] minion can be spotted (none is in play where it is read)
					put("keyward", "1_199");    // Troll's Keyward: a unique [moria] Orc, strength 8; its text is about Cave Troll's cost
					put("commander", "1_186");  // Guard Commander: a unique [moria] Orc, strength 7; a Skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc who is not unique, cost 6, strength 13; no text
					put("grima", "4_153");      // Grima, Son of Galmod: a unique [isengard] Man, strength 4; his text reaches companions
					                            // bearing a Shadow condition (none here)
					put("longdark", "1_188");   // The Long Dark: a unique [moria] condition; a site 4-5 move trigger and a Skirmish ability

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "6_77");
					put("balrog", "12_79");     // The Balrog, The Terror of Khazad-dum: [moria] Balrog, unique, cost 12, strength 17, site 4;
					                            // its text acts only at an underground site
					put("flame", "6_76");       // The Balrog, Terror of Flame and Shadow: [moria] Balrog, unique, cost 14; -3 to play
					                            // while a [moria] minion can be spotted (none is in play where it is read)
					put("keyward", "1_199");    // Troll's Keyward: a unique [moria] Orc, strength 8; its text is about Cave Troll's cost
					put("commander", "1_186");  // Guard Commander: a unique [moria] Orc, strength 7; a Skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc who is not unique, cost 6, strength 13; no text
					put("grima", "4_153");      // Grima, Son of Galmod: a unique [isengard] Man, strength 4; his text reaches companions
					                            // bearing a Shadow condition (none here)
					put("longdark", "1_188");   // The Long Dark: a unique [moria] condition; a site 4-5 move trigger and a Skirmish ability

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DurinsTowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Durin's Tower
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area. At sites 2[T] and 3[T], The Balrog's twilight cost is -3.
		 * <b>Skirmish:</b> Discard this condition to make a unique [moria] minion strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tower");

		assertEquals("Durin's Tower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheBalrogCostsThreeLessInHandAtSites2TAnd3TAndNowhereElseOnTheTowersPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var tower = scn.GetShadowCard("tower");
		var balrog = scn.GetShadowCard("balrog");
		var flame = scn.GetShadowCard("flame");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(balrog, flame, troop);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower);
		assertInHand(balrog, flame, troop);

		// Site 1T: the printed costs.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(12, scn.GetTwilightCost(balrog));
		assertEquals(14, scn.GetTwilightCost(flame));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.SkipToSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetTwilightCost(balrog));     // 12 - 3
		assertEquals(11, scn.GetTwilightCost(flame));     // 14 - 3
		assertEquals(6, scn.GetTwilightCost(troop));      // a [moria] minion of another title

		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetTwilightCost(balrog));
		assertEquals(11, scn.GetTwilightCost(flame));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(12, scn.GetTwilightCost(balrog));
		assertEquals(14, scn.GetTwilightCost(flame));
		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite2OfTheFellowshipBlockTheBalrogCostsWhatIsPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower);
		assertInHand(balrog);
		scn.SkipToSite(2);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(12, scn.GetTwilightCost(balrog));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite2TTheBalrogIsPlayedFromElevenTwilightAndNotFromTen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var tower = scn.GetShadowCard("tower");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower);
		assertInHand(balrog);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(balrog));

		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(balrog));
		scn.ShadowPlayCard(balrog);

		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		// 11 - (12 - 3 + 2 roaming at site 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsTheTowerToMakeAUniqueMoriaMinionStrengthPlusThreeForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var tower = scn.GetShadowCard("tower");
		var balrog = scn.GetShadowCard("balrog");
		var keyward = scn.GetShadowCard("keyward");
		var commander = scn.GetShadowCard("commander");
		var troop = scn.GetShadowCard("troop");
		var grima = scn.GetShadowCard("grima");
		var longdark = scn.GetShadowCard("longdark");
		scn.MoveCardsToSupportArea(tower, longdark);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower, longdark);
		scn.MoveMinionsToTable(balrog, keyward, commander, troop, grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, balrog}, new PhysicalCardImpl[]{boromir, keyward});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(keyward));
		assertTrue(scn.ShadowActionAvailable(tower));
		scn.ShadowUseCardAction(tower);

		assertInDiscard(tower);
		// The Balrog in this skirmish, the Keyward waiting for his with Boromir, and the unassigned Commander.
		assertTrue(scn.ShadowHasCardChoiceAvailable(balrog, keyward, commander));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, grima, longdark));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(keyward);

		assertEquals(11, scn.GetStrength(keyward));    // 8 + 3
		assertEquals(17, scn.GetStrength(balrog));
		assertEquals(7, scn.GetStrength(commander));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish it was given in, and is gone in the Keyward's own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(8, scn.GetStrength(keyward));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var keyward = scn.GetShadowCard("keyward");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower);
		scn.MoveMinionsToTable(keyward);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tower));
		assertInZone(Zone.SUPPORT, tower);
	}

	@Test
	public void WithNoUniqueMoriaMinionInPlayTheTowerIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tower = scn.GetShadowCard("tower");
		var troop = scn.GetShadowCard("troop");
		var grima = scn.GetShadowCard("grima");
		var longdark = scn.GetShadowCard("longdark");
		scn.MoveCardsToSupportArea(tower, longdark);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tower, longdark);
		scn.MoveMinionsToTable(troop, grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(grima));
		assertTrue(scn.ShadowActionAvailable(tower));
		scn.ShadowUseCardAction(tower);

		assertInDiscard(tower);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(grima));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
