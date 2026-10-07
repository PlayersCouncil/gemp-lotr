package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_080_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "103_80");
					put("nertea", "1_234");        // Ulaire Nertea: [ringwraith] Nazgul, vitality 2; her own text needs 5 companions
					put("lemenya", "1_232");       // Ulaire Lemenya: [ringwraith] Nazgul, cost 4, site 3; weakens bearers of [ringwraith] conditions
					put("khamul", "103_61");       // Ulaire Attea, Khamul of the East: a [raider] Nazgul; his text needs 3 burdens
					put("soldier", "7_199");       // Morgul Soldier: a [ringwraith] Orc and no Nazgul; "Nazgul are strength +1"
					put("swarms", "1_183");        // Goblin Swarms: Shadow condition in the support area
					put("poison", "8_28");         // Spider Poison: Shadow possession borne by a companion; idle until its bearer is assigned

					put("aragorn", "1_89");        // Aragorn, Ranger of the North: vitality 4
					put("boromir", "1_96");        // Boromir, Lord of Gondor: vitality 3
					put("athelas", "1_94");        // [gondor] possession borne by a Man; a fellowship ability
					put("toby", "1_305");          // Old Toby: [shire] possession in the support area
					put("sting", "1_313");         // [shire] possession borne by Frodo
					put("alliance", "1_49");       // The Last Alliance of Elves and Men: [elven] condition borne by a Man
					put("lordofmoria", "1_21");    // Lord of Moria: [dwarven] condition in the support area
					put("anduril", "7_79");        // Anduril, Flame of the West: [gondor] artifact borne by Aragorn
					put("coat", "2_105");          // Mithril-coat: [shire] artifact borne by Frodo
					put("silinde", "1_60");        // Silinde: [elven] ally, vitality 2
					put("bounder", "1_286");       // Bounder: [shire] ally, vitality 2
					put("army", "18_47");          // Elendil's Army: [gondor] follower
					put("stablemaster", "15_134"); // Rohan Stable Master: [rohan] follower; its bearer counts as mounted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Witch-king, Empowered by His Master
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Enduring. Damage +1.
		 * 	When you play this minion, spot another Nazgul to hinder all Free Peoples cards of one type (except
		 * 	companion). The Free Peoples player may restore any number of their cards, and must exert one of
		 * 	their characters for each card restored.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Empowered by His Master", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForTenPlusRoamingAndWithNoOtherNazgulNothingIsHinderedOrRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var toby = scn.GetFreepsCard("toby");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToSupportArea(toby, lordofmoria);

		scn.StartGame();
		// A card of the Free Peoples player's that is already hindered, which he might want back.
		scn.HinderCard(lordofmoria);
		// A [ringwraith] minion that is not a Nazgul.
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(24, scn.GetTwilight());
		assertInZone(Zone.HAND, witchking);
		assertTrue(scn.ShadowPlayAvailable(witchking));

		scn.ShadowPlayCard(witchking);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// 24 - 10 to play - 2 roaming (a site 3 minion played at site 2)
		assertEquals(12, scn.GetTwilight());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(lordofmoria));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnotherNazgulEveryFreePeoplesCardOfTheChosenTypeIsHinderedAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		var swarms = scn.GetShadowCard("swarms");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var toby = scn.GetFreepsCard("toby");
		var alliance = scn.GetFreepsCard("alliance");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var anduril = scn.GetFreepsCard("anduril");
		var coat = scn.GetFreepsCard("coat");
		var silinde = scn.GetFreepsCard("silinde");
		var army = scn.GetFreepsCard("army");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		// Two possessions, of two cultures, one borne and one in the support area.
		scn.AttachCardsTo(aragorn, athelas, alliance, anduril, poison);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToSupportArea(toby, lordofmoria, army);
		scn.MoveCardsToSupportArea(swarms);

		scn.StartGame();
		// The only other Nazgul is a [raider] one.
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);

		// The type is named by pointing at a card of it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(athelas, toby, alliance, lordofmoria, anduril, coat, silinde, army));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, ring, poison, swarms, khamul, witchking));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(toby));

		scn.ShadowChooseCard(toby);

		assertTrue(scn.IsHindered(athelas));
		assertTrue(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(alliance));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(silinde));
		assertFalse(scn.IsHindered(army));
		// A Shadow possession, the companions and The One Ring are left alone.
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(swarms));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(ring));

		// The Free Peoples player may have them back, and need not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(alliance, lordofmoria, anduril, coat, silinde, army, aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsDeclineChoosing();

		assertTrue(scn.IsHindered(athelas));
		assertTrue(scn.IsHindered(toby));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ConditionsHinderedAndACardHinderedEarlierIsRestoredForOneExertionByAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nertea = scn.GetShadowCard("nertea");
		var swarms = scn.GetShadowCard("swarms");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var athelas = scn.GetFreepsCard("athelas");
		var toby = scn.GetFreepsCard("toby");
		var alliance = scn.GetFreepsCard("alliance");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(aragorn, athelas, alliance);
		scn.MoveCardsToSupportArea(toby, lordofmoria);
		scn.MoveCardsToSupportArea(swarms);

		scn.StartGame();
		// Hindered before the Witch-king is played: a possession of the Free Peoples player's and a Shadow condition.
		scn.HinderCard(toby, swarms);
		// Vitality 3 with 2 wounds: Boromir cannot exert.
		scn.AddWoundsToChar(boromir, 2);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);

		// A card already hindered has no type to be named by.
		assertTrue(scn.ShadowHasCardChoiceAvailable(athelas, alliance, lordofmoria, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby, swarms));

		scn.ShadowChooseCard(alliance);

		assertTrue(scn.IsHindered(alliance));
		assertTrue(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(bounder));

		// "Their cards": every hindered card of his, the one hindered earlier included; no Shadow card, and
		// nothing that is not hindered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(alliance, lordofmoria, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(athelas, swarms, aragorn, bounder));
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(toby);

		assertFalse(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(alliance));
		assertTrue(scn.IsHindered(lordofmoria));
		assertTrue(scn.IsHindered(swarms));

		// One card restored, one exertion, by any character of his who can exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, nertea, witchking));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsChooseCard(bounder);

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AlliesHinderedAndTwoRestoredForTwoExertionsWhichMayBothFallOnOneCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var bounder = scn.GetFreepsCard("bounder");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, bounder);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasCardChoiceAvailable(silinde, bounder, athelas));
		scn.ShadowChooseCard(bounder);

		assertTrue(scn.IsHindered(silinde));
		assertTrue(scn.IsHindered(bounder));
		assertFalse(scn.IsHindered(athelas));

		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, bounder));
		scn.FreepsChooseCards(silinde, bounder);

		assertFalse(scn.IsHindered(silinde));
		assertFalse(scn.IsHindered(bounder));

		// The allies are restored before anyone exerts, so they may be the ones to do it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, silinde, bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, silinde, bounder));
		scn.FreepsChooseCard(aragorn);

		// Two cards restored, two exertions.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArtifactsHinderedAndTwoRestoredForExertionsSpreadOverTwoCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var coat = scn.GetFreepsCard("coat");
		var sting = scn.GetFreepsCard("sting");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, athelas);
		scn.AttachCardsTo(frodo, coat, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasCardChoiceAvailable(anduril, coat, sting, athelas));
		scn.ShadowChooseCard(anduril);

		assertTrue(scn.IsHindered(anduril));
		assertTrue(scn.IsHindered(coat));
		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(athelas));

		assertTrue(scn.FreepsHasCardChoiceAvailable(anduril, coat));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sting, athelas));
		scn.FreepsChooseCards(anduril, coat);

		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(coat));

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FollowersHinderedWhetherBorneOrInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var army = scn.GetFreepsCard("army");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, stablemaster, athelas);
		scn.MoveCardsToSupportArea(army);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, stablemaster, athelas));
		scn.ShadowChooseCard(army);

		assertTrue(scn.IsHindered(army));
		assertTrue(scn.IsHindered(stablemaster));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(army, stablemaster));
		scn.FreepsChooseCards(stablemaster);

		assertFalse(scn.IsHindered(stablemaster));
		assertTrue(scn.IsHindered(army));

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAnotherNazgulWhileHeIsInPlayHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyCompanionsOnTheOtherSideNothingIsHinderedAndNobodyIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(witchking);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(scn.GetRing()));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCharacterAbleToExertCardsMayStillBeRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(witchking);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		// Vitality 4 with 3 wounds: the only character on that side cannot exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);
		scn.ShadowChooseCard(sting);

		assertTrue(scn.IsHindered(sting));
		assertTrue(scn.IsHindered(toby));

		// The exertion is written after the restoring, as something that then has to be done; it is not
		// written as its price. With nobody able to exert, the cards come back all the same.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sting, toby));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sting, toby);

		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(toby));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
