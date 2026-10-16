package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_068_Tests
{

	// The Towers path: site 2 is Eastfold 4_331 (plains), and no site the fellowship reaches here is a forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "15_68");
					// Ranger of Osgiliath: [gondor] Man companion, ranger, strength 6; a regroup ability used only on
					// purpose
					put("ranger", "7_116");
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man companion, no ranger, strength 6; Knight is his whole text
					// Gondorian Merchant: the [gondor] Man ally, home 3T, strength 6, vitality 1; his maneuver
					// ability is used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, no Man (cheated in past his spot);
					// his skirmish ability is used only on purpose
					put("pippin", "103_31");
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion; Valiant is his whole text
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, site 4, strength 13, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, site 5, strength 5, vitality 3
					put("brigand", "4_10");       // Dunlending Brigand: [dunland] Man, site 3, strength 8, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Derndingle 4_330, a forest, as site 2.  Its move-limit text wants an unbound Hobbit, and
	// none is here.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "15_68");
					put("knight", "5_35");
					put("merchant", "7_97");
					// Aragorn, Ranger of the North: the Aragorn One Whom Men Would Follow exerts; his own maneuver
					// ability is used only on purpose
					put("aragorn", "1_89");
					// One Whom Men Would Follow: [gondor] Maneuver event; exert Aragorn, and an ally takes part in
					// skirmishes until the regroup phase
					put("onewhom", "1_109");
					put("troop", "1_177");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangersCloakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Ranger's Cloak
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Cloak
		 * Vitality: +1
		 * Resistance: +1
		 * Game Text: Bearer must be a [gondor] Man.
		 * 	While bearer is a ranger or at a forest site, each minion skirmishing bearer is site number +2.
		 * 	<b>Skirmish:</b> Add a threat or exert bearer to make a minion skirmishing a [gondor] Man site number +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cloak");

		assertEquals("Ranger's Cloak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.CLOAK));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void CanBePlayedOnAGondorManCompanionOrAllyButNotOnAGondorHobbitOrARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetRingBearer();
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(ranger, knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(cloak);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(cloak));
		scn.FreepsPlayCard(cloak);

		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(cloak, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileBearerIsARangerEachMinionSkirmishingHimIsSiteNumberPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(ranger, knight);
		scn.AttachCardsTo(ranger, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, brigand);
		scn.SkipToAssignments();

		// At Eastfold, a plains.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger, troop}, new PhysicalCardImpl[]{knight, brigand});
		// The Uruk Savage left over is the Shadow player's to assign: a second minion against the ranger.
		scn.ShadowAssignToMinions(ranger, savage);

		// Assigned to bearer, but his skirmish has not begun.
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));

		scn.FreepsResolveSkirmish(knight);

		// A minion skirmishing another [gondor] Man.
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(ranger);

		assertEquals(6, scn.GetMinionSiteNumber(troop));
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtAForestSiteEachMinionSkirmishingBearerWhoIsNoRangerIsSiteNumberPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));

		scn.FreepsAssignToMinions(knight, troop);
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByNoRangerAwayFromAForestNoMinionIsChangedNorIsOneSkirmishingAnotherRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger, knight);
		scn.AttachCardsTo(knight, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{ranger, savage});

		// A ranger who does not bear the cloak.
		scn.FreepsResolveSkirmish(ranger);
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByTheAllyWhoseHomeIsNoForestNoMinionSkirmishingHimIsChangedAtAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var onewhom = scn.GetFreepsCard("onewhom");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, cloak);
		scn.MoveCardsToHand(onewhom);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The fellowship is at Derndingle, a forest; the Merchant's home is 3T, Barrows of Edoras, which is none.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));

		// Set-up: the Merchant takes part in skirmishes.
		scn.FreepsPlayCard(onewhom);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merchant, troop);
		scn.FreepsResolveSkirmish(merchant);

		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionAddsAThreatToMakeAMinionSkirmishingAGondorManSiteNumberPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(ranger, knight);
		// The Knight is no ranger and Eastfold no forest, so the cloak's second sentence is off.
		scn.AttachCardsTo(knight, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{ranger, brigand});
		scn.ShadowAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);

		assertEquals(List.of("Add a threat", "Exert bearer"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(knight));
		// The two minions in this skirmish, and not the one assigned to the ranger, whose skirmish is to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(6, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExertingBearerInAnotherGondorMansSkirmishMakesHisMinionSiteNumberPlusTwoForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(ranger, knight);
		scn.AttachCardsTo(knight, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger, savage}, new PhysicalCardImpl[]{knight, brigand});
		scn.FreepsResolveSkirmish(ranger);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		scn.FreepsUseCardAction(cloak);
		scn.FreepsChoose("Exert bearer");

		// The Savage is the only minion in this skirmish, and is chosen without a question.
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The ranger (6) beats the Savage (5), which takes a wound and lives.  In the next skirmish it is back to
		// its printed number.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExertingAnAllyBearerInHisOwnSkirmishMakesAMinionSkirmishingHimSiteNumberPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var merchant = scn.GetFreepsCard("merchant");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, cloak);

		scn.StartGame();
		// The Merchant's home is site 3 of the Towers path, where he can be assigned; Barrows of Edoras is no forest,
		// and he is no ranger, so the cloak's second sentence is off.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merchant, troop);
		scn.ShadowAssignToMinions(merchant, savage);
		scn.FreepsResolveSkirmish(merchant);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));

		scn.FreepsUseCardAction(cloak);
		scn.FreepsChoose("Exert bearer");

		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		// Both minions are in the ally's skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);

		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithThreatsAtTheLimitBearerExertsUnaskedAndOnceExhaustedTooTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger, knight);
		scn.AttachCardsTo(knight, cloak);

		scn.StartGame();
		// Three companions: three threats is the limit.
		scn.AddThreats(3);
		// Vitality 3, +1 from the cloak: one exertion more exhausts him.
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.ShadowAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);

		// No choice of cost is asked: only the exertion can be paid.
		assertEquals(3, scn.GetWoundsOn(knight));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);

		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		// Now neither cost can be paid.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cloak));
	}

	@Test
	public void InTheSkirmishOfARohanManOrOfAGondorHobbitTheThreatIsAddedAndNoMinionIsChanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.AttachCardsTo(knight, cloak);

		scn.StartGame();
		// Vitality 3, +1 from the cloak: exhausted, so the only cost that can be paid is the threat.
		scn.AddWoundsToChar(knight, 3);
		scn.MoveMinionsToTable(savage, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, brigand}, new PhysicalCardImpl[]{pippin, savage});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);

		// A Man who is not [gondor]: the threat is paid, and there is no minion to choose.
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetWoundsOn(knight));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);

		// A [gondor] companion who is no Man: the same.
		assertEquals(2, scn.GetThreats());
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
