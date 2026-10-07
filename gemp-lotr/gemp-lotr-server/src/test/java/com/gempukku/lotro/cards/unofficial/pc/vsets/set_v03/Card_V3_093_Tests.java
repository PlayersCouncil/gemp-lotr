package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_093_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("despair", "103_93");
					put("despair2", "103_93");
					put("sky1", "103_97");        // Ominous Sky: a [sauron] twilight condition, cost 2, nothing needed to play it; its texts answer the play of an Orc or Troll and cost (2) in the Shadow or regroup phase
					put("sky2", "103_97");
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear: a [ringwraith] twilight condition; its ability is an assignment action that needs 3 [ringwraith] minions
					put("servants", "1_219");     // The Nine Servants of Sauron: a [ringwraith] condition that is not twilight
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: a twilight card that is no condition
					put("udun", "6_106");         // Troll of Udun: a [sauron] Troll, vitality 5; fierce; his skirmish ability discards a [sauron] card from hand
					put("cavetroll", "1_165");    // Cave Troll of Moria, Scourge of the Black Pit: a [moria] Troll, vitality 4; damage +1, fierce
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: no skirmish ability
					put("gimli", "1_13");         // Gimli, Son of Gloin: Skirmish: exert Gimli to make him strength +2
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: an ally whose home is site 3
					put("anduril", "7_79");       // Anduril, Flame of the West: a [gondor] artifact; bearer must be Aragorn
					put("athelas", "1_94");       // Athelas: a [gondor] possession; a fellowship ability
					put("alliance", "1_49");      // The Last Alliance of Elves and Men: an [elven] condition; strength +1 for each Elf
					put("armor", "1_8");          // Dwarven Armor: a [dwarven] possession; bearer must be a Dwarf
					put("vilya", "3_27");         // Vilya: an [elven] artifact; bearer must be Elrond; a maneuver ability
					put("sword", "1_299");        // Hobbit Sword: a [shire] possession with no text
					put("tale", "1_317");         // There and Back Again: a [shire] condition a Hobbit companion bears; a maneuver ability
					put("breath", "1_207");       // Black Breath: a Shadow condition that a character bears
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Despair
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Skirmish: Exert a Troll twice and hinder this condition to discard a Free Peoples card borne by a
		 * 	companion that Troll is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("despair");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Despair", card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayHindersTwoTwilightConditionsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(despair);
		// Twilight conditions: two that are [sauron], and a Cover of Darkness that is [ringwraith].
		// And a condition that is not twilight.
		scn.MoveCardsToSupportArea(sky1, sky2, fear, servants);

		scn.StartGame();
		// A twilight card that is no condition
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(despair));

		scn.ShadowPlayCard(despair);

		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, sky2, fear));
		assertFalse(scn.ShadowHasCardChoiceAvailable(servants));
		assertFalse(scn.ShadowHasCardChoiceAvailable(nelya));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(fear));

		scn.ShadowChooseCards(sky1, fear);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 11 - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, despair);
		assertFalse(scn.IsHindered(despair));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFewerThanTwoTwilightConditionsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(despair, sky2);
		// One twilight condition that can be hindered. Beside it: one that is hindered already, a condition
		// that is not twilight, and (below) a twilight minion.
		scn.MoveCardsToSupportArea(sky1, fear, servants);
		scn.HinderCard(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(despair));

		// A second twilight condition, played from hand
		scn.ShadowPlayCard(sky2);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(despair));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		// With exactly two there is nothing to choose.
		scn.ShadowPlayCard(despair);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 9 - 2 to play
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, despair);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionExertsATrollTwiceAndHindersItselfToDiscardAFreePeoplesCardOnTheCompanionThatTrollIsFighting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var despair2 = scn.GetShadowCard("despair2");
		var udun = scn.GetShadowCard("udun");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var troop = scn.GetShadowCard("troop");
		var breath = scn.GetShadowCard("breath");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");
		var alliance = scn.GetFreepsCard("alliance");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(despair, despair2);
		scn.MoveCompanionsToTable(aragorn, gimli);
		// An artifact, a possession and a condition of two cultures, and a Shadow card
		scn.AttachCardsTo(aragorn, anduril, athelas, alliance, breath);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		// Trolls of two cultures, and an Orc with the vitality to exert twice
		scn.MoveMinionsToTable(udun, cavetroll, troop);

		// Not an assignment action
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(despair));
		scn.ShadowPass();

		// Aragorn fights one Troll; Gimli is assigned to the other and waits his turn.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, udun}, new PhysicalCardImpl[]{gimli, cavetroll});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(despair));
		assertTrue(scn.ShadowActionAvailable(despair2));
		assertEquals(0, scn.GetWoundsOn(udun));
		assertEquals(0, scn.GetWoundsOn(cavetroll));
		assertFalse(scn.IsHindered(despair));

		scn.ShadowUseCardAction(despair);

		assertTrue(scn.ShadowHasCardChoiceAvailable(udun, cavetroll));
		assertFalse(scn.ShadowHasCardChoiceAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(udun);

		assertEquals(2, scn.GetWoundsOn(udun));
		assertEquals(0, scn.GetWoundsOn(cavetroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.IsHindered(despair));
		assertFalse(scn.IsHindered(despair2));

		// Free Peoples cards Aragorn bears: not the Shadow card on him, not what Gimli bears, not Aragorn.
		assertTrue(scn.ShadowHasCardChoiceAvailable(anduril, athelas, alliance));
		assertFalse(scn.ShadowHasCardChoiceAvailable(breath));
		assertFalse(scn.ShadowHasCardChoiceAvailable(armor));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertAttachedTo(athelas, aragorn);

		scn.ShadowChooseCard(athelas);

		assertInDiscard(athelas);
		assertAttachedTo(anduril, aragorn);
		assertAttachedTo(alliance, aragorn);
		assertAttachedTo(breath, aragorn);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The copy that was used is hindered; the other is still there to use.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(despair));
		assertTrue(scn.ShadowActionAvailable(despair2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TrollWaitingForHisOwnSkirmishMayPayAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var udun = scn.GetShadowCard("udun");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var athelas = scn.GetFreepsCard("athelas");
		var alliance = scn.GetFreepsCard("alliance");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(despair);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, athelas, alliance);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(udun, cavetroll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, udun}, new PhysicalCardImpl[]{gimli, cavetroll});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(despair));
		scn.ShadowUseCardAction(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(udun, cavetroll));
		// The Troll assigned to Gimli, whose skirmish has not begun: he is skirmishing nobody yet.
		scn.ShadowChooseCard(cavetroll);

		assertEquals(2, scn.GetWoundsOn(cavetroll));
		assertEquals(0, scn.GetWoundsOn(udun));
		assertTrue(scn.IsHindered(despair));
		// Neither what the companion he is assigned to bears, nor what the companion now fighting bears
		assertAttachedTo(armor, gimli);
		assertAttachedTo(athelas, aragorn);
		assertAttachedTo(alliance, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FreePeoplesCardBorneByAnAllyFightingTheTrollIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var udun = scn.GetShadowCard("udun");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");

		scn.StartGame();
		// Elrond's home is site 3: he may fight there.
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCardsToSupportArea(despair);
		scn.MoveMinionsToTable(udun);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, udun);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(despair));
		assertEquals(0, scn.GetWoundsOn(udun));

		scn.ShadowUseCardAction(despair);

		// The costs are paid, and an ally is not a companion.
		assertEquals(2, scn.GetWoundsOn(udun));
		assertTrue(scn.IsHindered(despair));
		assertAttachedTo(vilya, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RingBearerFightingTheTrollLosesAFreePeoplesCardAndNeverTheOneRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var despair = scn.GetShadowCard("despair");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var sword = scn.GetFreepsCard("sword");
		var tale = scn.GetFreepsCard("tale");

		scn.MoveCardsToSupportArea(despair);
		// A possession and a condition
		scn.AttachCardsTo(frodo, sword, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(cavetroll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, cavetroll);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		scn.ShadowUseCardAction(despair);

		assertEquals(2, scn.GetWoundsOn(cavetroll));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, tale));
		// The One Ring has no side.
		assertFalse(scn.ShadowHasCardChoiceAvailable(ring));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(tale);

		assertInDiscard(tale);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(ring, frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Ignore("Q-0098: may 'a Free Peoples card' be a hindered one? CR 5.0 says a hindered card is still targeted by its side; the game leaves it out of the choice")
	@Test
	public void HinderedFreePeoplesCardOnThatCompanionMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var udun = scn.GetShadowCard("udun");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var alliance = scn.GetFreepsCard("alliance");

		scn.MoveCardsToSupportArea(despair);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas, alliance);
		scn.HinderCard(athelas);

		scn.StartGame();
		scn.MoveMinionsToTable(udun);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, udun);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(athelas));
		scn.ShadowUseCardAction(despair);

		assertEquals(2, scn.GetWoundsOn(udun));
		// Hindered, it has no type or culture, but it is still a Free Peoples card and Aragorn still bears it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(athelas, alliance));
		scn.ShadowChooseCard(athelas);

		assertInDiscard(athelas);
		assertAttachedTo(alliance, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithAnOrcAndNoTroll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");

		scn.MoveCardsToSupportArea(despair);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// An Orc who could exert twice, fighting a companion who bears a Free Peoples card
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.IsHindered(despair));
		assertAttachedTo(athelas, aragorn);
		assertFalse(scn.ShadowActionAvailable(despair));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedUntilATrollCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var despair = scn.GetShadowCard("despair");
		var udun = scn.GetShadowCard("udun");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCardsToSupportArea(despair);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(udun);
		// Vitality 5 with 3 wounds: he can exert once, not twice.
		scn.AddWoundsToChar(udun, 3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, udun);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(despair));

		scn.ShadowPass();
		// One wound fewer, and Gimli acts again so that the Shadow player is asked anew.
		scn.RemoveWoundsFromChar(udun, 1);
		scn.FreepsUseCardAction(gimli);

		assertEquals(2, scn.GetWoundsOn(udun));
		assertTrue(scn.ShadowActionAvailable(despair));

		scn.ShadowUseCardAction(despair);

		assertEquals(4, scn.GetWoundsOn(udun));
		assertTrue(scn.IsHindered(despair));
		assertInDiscard(armor);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
