package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_141_Tests
{

	// Sauron is strength 24 + 2 with the Sceptre (26), damage +1 + 1: a companion he beats takes 3 wounds, and one
	// of strength 13 or less is overwhelmed.  The companion he fights is given strength by hand (14: not
	// overwhelmed, 26 against 28) so that he lives through the loss.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sceptre", "13_141");

					put("sauron", "13_140");    // Sauron, Dark Lord of Mordor: strength 24, vitality 5, damage +1, fierce; his play text never runs (cheated in)
					put("trooper", "3_99");     // Orc Trooper: [sauron] Orc, strength +1 for each card in the Shadow hand

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, vitality 3; damage +1, a skirmish ability never used
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion, vitality 4; a fellowship ability never used
					put("bounder", "1_286");    // Bounder: Hobbit ALLY, home 2F; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SceptreoftheDarkLordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Sceptre of the Dark Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: Bearer must be Sauron.<br>He is <b>damage +1</b>.<br>Each time Sauron wins a skirmish and the companion he is skirmishing is not killed, you may assign X wounds to other companions, where X is the current region number.
		*/

		var scn = GetScenario();

		var sceptre = scn.GetFreepsCard("sceptre");

		assertEquals("Sceptre of the Dark Lord", sceptre.getBlueprint().getTitle());
		assertNull(sceptre.getBlueprint().getSubtitle());
		assertTrue(sceptre.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sceptre.getBlueprint().getSide());
		assertEquals(Culture.SAURON, sceptre.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, sceptre.getBlueprint().getCardType());
		assertTrue(sceptre.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, sceptre.getBlueprint().getTwilightCost());
		assertEquals(2, sceptre.getBlueprint().getStrength());
		assertEquals(1, sceptre.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedWithNoSauronToBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCardsToHand(sceptre);

		scn.StartGame();
		assertInHand(sceptre);
		scn.MoveMinionsToTable(trooper);
		scn.FreepsPass();

		// A [sauron] minion who is not Sauron.
		assertInPlay(trooper);
		assertInHand(sceptre);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sceptre));
	}

	@Test
	public void PlayedOnSauronItMakesHimDamagePlusOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var sauron = scn.GetShadowCard("sauron");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCardsToHand(sceptre);

		scn.StartGame();
		assertInHand(sceptre);
		scn.MoveMinionsToTable(sauron, trooper);
		scn.FreepsPass();

		assertInHand(sceptre);
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(trooper, Keyword.DAMAGE));

		// Sauron is the one bearer it can go on, and it goes on him unasked; not on the Orc Trooper.
		assertTrue(scn.ShadowPlayAvailable(sceptre));
		scn.ShadowPlayCard(sceptre);

		assertAttachedTo(sceptre, sauron);
		// Printed damage +1, and +1 from the Sceptre
		assertEquals(2, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(trooper, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegionOneWhenSauronWinsAndTheCompanionLivesHeMayAssignOneWoundToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var sauron = scn.GetShadowCard("sauron");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);
		// Gimli is exhausted: the wound kills him.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(sauron);
		scn.AttachCardsTo(sauron, sceptre);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(26, scn.GetStrength(sauron));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.PassSkirmishActions();

		// 1 for losing + 2 damage: Aragorn lives with one vitality left.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInPlay(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sceptre));
		scn.ShadowAcceptOptionalTrigger();

		// Site 2: region 1, one wound.  Other companions (of three cultures, one exhausted); not the companion he
		// skirmished, an ally, or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, sam, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, bounder, sauron));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		// Sauron is fierce: the fierce assignment phase follows.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void InRegionTwoBothWoundsMayGoOnOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var sauron = scn.GetShadowCard("sauron");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		scn.StartGame();
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(sauron);
		scn.AttachCardsTo(sauron, sceptre);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassSkirmishActions();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sceptre));
		scn.ShadowAcceptOptionalTrigger();

		// Site 4: region 2, two wounds, which may both go on one companion (Q-0345).
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, sam, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(sam);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, sam, frodo));
		scn.ShadowChooseCard(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhenTheCompanionHeBeatsIsKilledByTheWoundsItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		// One wound already: the three from losing kill him.
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(sauron);
		scn.AttachCardsTo(sauron, sceptre);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(14, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sceptre));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhenSauronLosesTheSkirmishItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		scn.StartGame();
		// 28 against Sauron's 26: Aragorn wins, and is no double of Sauron.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 20));
		scn.MoveMinionsToTable(sauron);
		scn.AttachCardsTo(sauron, sceptre);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(28, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(sauron));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(sauron));
		assertInPlay(sauron, aragorn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sceptre));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedNoWoundIsAssignedAndInTheFierceRoundItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var sauron = scn.GetShadowCard("sauron");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		scn.StartGame();
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		// 3 + 11: Sam, too, is beaten by Sauron and not overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 11));
		scn.MoveMinionsToTable(sauron);
		scn.AttachCardsTo(sauron, sceptre);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sceptre));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, sauron);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(14, scn.GetStrength(sam));
		scn.PassFierceSkirmishActions();

		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sceptre));
		scn.ShadowAcceptOptionalTrigger();
		// Aragorn, out of this skirmish, is another companion now.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, aragorn, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam));
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
