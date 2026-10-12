package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strands", "103_14");
					put("strands2", "103_14");
					put("strands3", "103_14");
					put("strands4", "103_14");
					put("burden", "7_60");          // Heavy Burden: [gollum] Shadow condition in the support area; a regroup ability only
					put("burden2", "7_60");
					put("burden3", "7_60");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum], strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: a [wraith] Gollum, strength 5, vitality 4; an assignment ability never used here
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: [gollum], strength 8, vitality 8, enduring, fierce; damage +1 in a fierce skirmish
					put("runner", "1_178");         // Goblin Runner: [moria], strength 5, vitality 1

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("glorfindel", "9_16");      // Glorfindel, Revealed in Wrath: strength 9, vitality 3; a skirmish ability never used here
					put("sam", "1_311");            // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability never used here
					put("smeagol", "5_29");         // Smeagol, Slinker: a [gollum] card of the other player's; a skirmish ability never used here
					put("elrond", "1_40");          // Elrond, Lord of Rivendell: ally, home site 3, strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StickyStrandsofSpidersilkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sticky Strands of Spider-silk
		 * Unique: false
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Strength: -1
		 * Game Text: At the end of each skirmish involving bearer, make bearer strength -1 until the end of the turn.
		 * 	Skirmish: Spot a companion skirmishing a [Gollum] minion and hinder your [gollum] card to transfer this to that companion. Limit 1 per bearer unless they are skirmishing Shelob.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strands");

		assertEquals("Sticky Strands of Spider-silk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	@Test
	public void HinderingAGollumCardTransfersItToTheCompanionFightingGollumWhoIsWeakerAtOnceAndWeakerAgainWhenTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var burden = scn.GetShadowCard("burden");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveCardsToSupportArea(strands, burden);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, strands);
		assertFalse(scn.IsHindered(burden));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(strands));

		scn.ShadowUseCardAction(strands);
		scn.ShadowChooseCard(burden);

		assertTrue(scn.IsHindered(burden));
		assertAttachedTo(strands, aragorn);
		// Glorfindel is assigned to a [gollum] minion too, but his skirmish has not begun.
		assertNotAttachedTo(strands, glorfindel);
		// A transfer by a card's effect does not pay the twilight cost.
		assertEquals(twilight, scn.GetTwilight());
		// 8 - 1 printed on the Strands
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(glorfindel));

		// Aragorn (7) still beats Gollum (5); the skirmish he bore the Strands in has ended.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		// 8 - 1 printed - 1 for the skirmish
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(glorfindel));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void EachSkirmishOfItsBearerTakesOneMoreOffHimUntilTheTurnEndsAndEachCopyOnlyOffItsOwnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var strands2 = scn.GetShadowCard("strands2");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.AttachCardsTo(aragorn, strands);
		scn.AttachCardsTo(glorfindel, strands2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner);
		scn.SkipToAssignments();

		// 8 - 1 and 9 - 1: the printed -1 of the copy each bears.
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(glorfindel));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, shelob },
				new PhysicalCardImpl[] { glorfindel, runner }
		);

		// Glorfindel's skirmish: his copy takes one off him, and Aragorn's copy does nothing.
		scn.FreepsResolveSkirmish(glorfindel);
		assertEquals(8, scn.GetStrength(glorfindel));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(7, scn.GetStrength(glorfindel));
		assertEquals(7, scn.GetStrength(aragorn));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// Aragorn's skirmish against Shelob (7 against 8): one off Aragorn, nothing more off Glorfindel,
		// and nothing off the minion he fought.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(glorfindel));
		assertEquals(8, scn.GetStrength(shelob));

		// Shelob is fierce: Aragorn's second skirmish of the turn takes another one off.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(aragorn));
		scn.PassFierceSkirmishActions();
		// 1 wound + 1 for losing + 1 for Shelob's damage +1 in a fierce skirmish
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(glorfindel));

		// Still in force in the regroup phase of the same turn.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(glorfindel));

		// The other player's turn, and then the start of this player's next: each still bears his copy
		// and has only its printed -1 against him.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(strands, aragorn);
		assertAttachedTo(strands2, glorfindel);
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(glorfindel));
	}

	@Test
	public void AnyUnhinderedGollumCardOfMineMayBeHinderedForItTheStrandsThemselvesIncludedAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var strands2 = scn.GetShadowCard("strands2");
		var burden = scn.GetShadowCard("burden");
		var burden2 = scn.GetShadowCard("burden2");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");

		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToSupportArea(strands, strands2, burden, burden2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.HinderCard(burden2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(strands));
		scn.ShadowUseCardAction(strands);

		// The minion in the skirmish, a condition, another copy, and the Strands being transferred.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, burden, strands2, strands));
		// A minion of mine of another culture, a [gollum] card already hindered, the other player's
		// [gollum] companion, and the companion in the skirmish.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, burden2, smeagol, aragorn));
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());

		// The Strands pay for their own transfer: a card hindered while its ability is being carried out
		// still finishes it.
		scn.ShadowChooseCard(strands);
		assertTrue(scn.IsHindered(strands));
		assertAttachedTo(strands, aragorn);
		assertFalse(scn.IsHindered(gollum));
		assertFalse(scn.IsHindered(burden));
		assertFalse(scn.IsHindered(strands2));
		assertInZone(Zone.SUPPORT, strands2);

		// Hindered, they are borne and do nothing: no printed -1, and nothing at the end of the skirmish.
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyACompanionInASkirmishAgainstAMinionOfTheGollumCultureCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var burden = scn.GetShadowCard("burden");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// Site 3 is Elrond's home, where an ally may be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(strands, burden);
		scn.MoveMinionsToTable(gollum, shelob, runner);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, runner },
				new PhysicalCardImpl[] { elrond, shelob },
				new PhysicalCardImpl[] { glorfindel, gollum }
		);

		// A companion against a [moria] minion.  Glorfindel is assigned to Gollum throughout, and his
		// skirmish is the last to begin.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertTrue(scn.IsCharAssignedAgainst(glorfindel, gollum));
		assertFalse(scn.ShadowActionAvailable(strands));
		scn.ShadowPass();

		// An ally against a [gollum] minion.
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, shelob));
		assertFalse(scn.ShadowActionAvailable(strands));
		scn.ShadowPass();

		// A companion against a [gollum] minion.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(glorfindel, gollum));
		assertTrue(scn.ShadowActionAvailable(strands));
		scn.ShadowUseCardAction(strands);
		scn.ShadowChooseCard(burden);

		assertTrue(scn.IsHindered(burden));
		assertAttachedTo(strands, glorfindel);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionSkirmishingAGollumWhoIsNoGollumCultureMinionCannotBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var burden = scn.GetShadowCard("burden");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var sam = scn.GetFreepsCard("sam");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(sam, glorfindel);
		scn.MoveCardsToSupportArea(strands, burden);

		scn.StartGame();
		// The one Gollum on this table is the [wraith] minion; Shelob is the [gollum] minion beside him.
		scn.MoveMinionsToTable(wraithgollum, shelob);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { sam, wraithgollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);

		// A companion against a minion named Gollum who is a [wraith] minion: a culture is asked for, not a
		// name.  Glorfindel is assigned to a [gollum] minion, and his skirmish has not begun.
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(sam, wraithgollum));
		assertTrue(scn.IsCharAssignedAgainst(glorfindel, shelob));
		assertFalse(scn.ShadowActionAvailable(strands));
		scn.ShadowPass();

		// The same Strands on the same table, once the companion against the [gollum] minion is skirmishing.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(glorfindel, shelob));
		assertTrue(scn.ShadowActionAvailable(strands));
		scn.ShadowUseCardAction(strands);
		scn.ShadowChooseCard(burden);

		assertTrue(scn.IsHindered(burden));
		assertAttachedTo(strands, glorfindel);
		assertNotAttachedTo(strands, sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionBearingOneIsGivenNoSecondWhileFightingGollumAndAnyNumberWhileFightingShelob() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var strands2 = scn.GetShadowCard("strands2");
		var strands3 = scn.GetShadowCard("strands3");
		var strands4 = scn.GetShadowCard("strands4");
		var burden = scn.GetShadowCard("burden");
		var burden2 = scn.GetShadowCard("burden2");
		var burden3 = scn.GetShadowCard("burden3");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.AttachCardsTo(aragorn, strands);
		scn.AttachCardsTo(glorfindel, strands2);
		scn.MoveCardsToSupportArea(strands3, strands4, burden, burden2, burden3);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);

		// Aragorn bears one copy and is fighting Gollum.  The ability has a cost and is still offered;
		// the cost is paid and no second copy reaches him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(strands3));
		scn.ShadowUseCardAction(strands3);
		scn.ShadowChooseCard(burden);
		assertTrue(scn.IsHindered(burden));
		assertInZone(Zone.SUPPORT, strands3);
		assertNotAttachedTo(strands3, aragorn);
		assertNotAttachedTo(strands3, glorfindel);
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Glorfindel bears one copy too, and is fighting Shelob: a second arrives, and a third.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();
		// 9 - 1 printed
		assertEquals(8, scn.GetStrength(glorfindel));
		assertTrue(scn.ShadowActionAvailable(strands3));
		scn.ShadowUseCardAction(strands3);
		scn.ShadowChooseCard(burden2);
		assertTrue(scn.IsHindered(burden2));
		assertAttachedTo(strands3, glorfindel);
		assertEquals(7, scn.GetStrength(glorfindel));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(strands4));
		scn.ShadowUseCardAction(strands4);
		scn.ShadowChooseCard(burden3);
		assertTrue(scn.IsHindered(burden3));
		assertAttachedTo(strands4, glorfindel);
		assertAttachedTo(strands3, glorfindel);
		assertAttachedTo(strands2, glorfindel);
		assertEquals(6, scn.GetStrength(glorfindel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByACompanionItCanBeMovedOnToAnotherCompanionWhoIsFightingAGollumMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var burden = scn.GetShadowCard("burden");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.AttachCardsTo(aragorn, strands);
		scn.MoveCardsToSupportArea(burden);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(glorfindel, gollum);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertAttachedTo(strands, aragorn);
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(glorfindel));
		assertTrue(scn.ShadowActionAvailable(strands));

		scn.ShadowUseCardAction(strands);
		scn.ShadowChooseCard(burden);

		assertTrue(scn.IsHindered(burden));
		assertAttachedTo(strands, glorfindel);
		assertNotAttachedTo(strands, aragorn);
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(glorfindel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PaidForByHinderingTheVeryMinionInTheSkirmishItStillGoesToTheCompanionWhoWasSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(strands));
		scn.ShadowUseCardAction(strands);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, strands));
		scn.ShadowChooseCard(gollum);

		// Aragorn was spotted before Gollum was hindered, and is still "that companion".  Hindered, Gollum
		// leaves the skirmish, which is over; the Strands go to Aragorn all the same.
		assertTrue(scn.IsHindered(gollum));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertAttachedTo(strands, aragorn);
		// 8 - 1 printed - 1 for his skirmish, which has ended with the Strands on him
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PaidForByHinderingTheVeryMinionInTheSkirmishNoSecondCopyGoesToACompanionWhoBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var strands2 = scn.GetShadowCard("strands2");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, strands);
		scn.MoveCardsToSupportArea(strands2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 8 - 1 printed
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(strands2));
		scn.ShadowUseCardAction(strands2);
		scn.ShadowChooseCard(gollum);

		// The companion who was spotted is still the one it would go to, and the limit is still his: he
		// bears a copy and was fighting Gollum, so the cost is paid and the second copy stays where it was.
		assertTrue(scn.IsHindered(gollum));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertInZone(Zone.SUPPORT, strands2);
		assertNotAttachedTo(strands2, aragorn);
		assertAttachedTo(strands, aragorn);
		// 8 - 1 printed - 1 for the skirmish that has ended: one copy's worth
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
