package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "5_33");
					put("wall2", "5_33");    // a second City Wall: the card is not unique
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, knight, strength 6, vitality 3, no other text
					// Knight of Gondor: [gondor] Man, knight, strength 5, vitality 3; a fellowship ability, never used
					put("kog", "5_36");
					// Ranger of Osgiliath: [gondor] Man who is no knight, strength 6, vitality 3; a regroup ability, never used
					put("ranger", "7_116");
					// Pippin, Trollslayer: a [gondor] Hobbit, and a knight who is no Man; his skirmish ability is never used
					put("pippin", "103_31");
					// Ceorl, Weary Horseman: a [rohan] Man; his response is about sites and never comes up
					put("ceorl", "4_264");

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CityWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: City Wall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Strength: -3
		 * Game Text: <b>Fortification</b>. Plays to your support area.
		 * <b>Skirmish:</b> Exert a [gondor] Man or spot 3 knights to transfer this condition from your support area
		 * to a minion skirmishing a [gondor] Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wall");

		assertEquals("City Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-3, card.getBlueprint().getStrength());
	}

	@Test
	public void ExertingAGondorManTransfersThatCopyToAMinionInTheSkirmishAndThenOnlyTheOtherCopyIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var wall2 = scn.GetFreepsCard("wall2");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var ceorl = scn.GetFreepsCard("ceorl");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");
		// Two knights only (the Gondorian Knight and Pippin): spotting 3 is not open.
		scn.MoveCompanionsToTable(knight, ranger, pippin, ceorl);
		scn.MoveCardsToSupportArea(wall, wall2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wall);
		assertInZone(Zone.SUPPORT, wall2);
		scn.MoveMinionsToTable(savage, brigand, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{ranger, troop});
		scn.ShadowAssignToMinions(knight, brigand);
		scn.FreepsResolveSkirmish(knight);

		assertInZone(Zone.SUPPORT, wall);
		assertInZone(Zone.SUPPORT, wall2);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertTrue(scn.FreepsActionAvailable(wall));
		assertTrue(scn.FreepsActionAvailable(wall2));

		scn.FreepsUseCardAction(wall);
		// Who exerts: a [gondor] Man in the skirmish or out of it; not a [gondor] Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, ceorl, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ranger);
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(knight));

		// Where it goes: either minion skirmishing the Gondorian Knight; not the Troop, assigned to the Ranger,
		// whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// Only the copy used moves; the other City Wall stays in the support area.
		assertAttachedTo(wall, savage);
		assertInZone(Zone.SUPPORT, wall2);
		assertTrue(scn.GetAttachedCards(brigand).isEmpty());
		assertTrue(scn.GetAttachedCards(troop).isEmpty());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Ranger's skirmish: he could exert again, but the first copy is no longer in the support area; the
		// second copy still is, and is offered.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(ranger);
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertAttachedTo(wall, savage);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(wall));
		assertTrue(scn.FreepsActionAvailable(wall2));
	}

	@Test
	public void AMinionSkirmishingAGondorHobbitOrARohanManIsNoTarget() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var ceorl = scn.GetFreepsCard("ceorl");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight, ranger, pippin, ceorl);
		scn.MoveCardsToSupportArea(wall);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wall);
		scn.MoveMinionsToTable(savage, brigand, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{ceorl, brigand},
				new PhysicalCardImpl[]{knight, troop});

		// Pippin's skirmish: he is [gondor] but no Man. The Ranger can pay, and pays for nothing.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		scn.FreepsChooseCard(ranger);
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();

		// Ceorl's skirmish: a Man, but [rohan]. The same.
		scn.FreepsResolveSkirmish(ceorl);
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		scn.FreepsChooseCard(ranger);
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.GetAttachedCards(brigand).isEmpty());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();

		// The Gondorian Knight's skirmish: the one minion there takes it. The Ranger is exhausted, so the Knight
		// is the one [gondor] Man who can exert, and nobody is asked.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsUseCardAction(wall);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertAttachedTo(wall, troop);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SpottingThreeKnightsTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight, kog, pippin);
		scn.MoveCardsToSupportArea(wall);

		scn.StartGame();
		// Both [gondor] Men exhausted: no [gondor] Man can exert. Pippin can, but is no Man.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(kog, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertInZone(Zone.SUPPORT, wall);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.ShadowAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		// Three knights: two [gondor] Men and a Hobbit.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);

		assertAttachedTo(wall, troop);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithEveryGondorManExhaustedAndOnlyTwoKnights() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight, kog, ranger);
		scn.MoveCardsToSupportArea(wall);

		scn.StartGame();
		// Three [gondor] Men, all exhausted; two of them knights, and the Ranger no knight.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(kog, 2);
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, wall);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(wall));
		assertInZone(Zone.SUPPORT, wall);
	}
}
