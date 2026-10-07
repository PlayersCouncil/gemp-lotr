package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_005_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blow", "51_5");

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no damage bonus
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2
					put("soldier2", "1_271");
					put("lemenya", "1_232");      // Ulaire Lemenya: Nazgul, strength 9; his text needs a [wraith] condition

					put("blade1", "2_95");        // Vile Blade: Shadow possession on a [sauron] Orc; +2 only against a Man or Elf
					put("blade2", "2_95");
					put("rancor", "9_44");        // Ring of Rancor: Shadow artifact on a Nazgul; a maneuver ability and a response to a reconcile
					put("enraged", "1_254");      // Mordor Enraged: Shadow condition on a [sauron] minion; a Shadow-phase ability only
					put("poison", "8_28");        // Spider Poison: Shadow possession a companion bears; bearer exerts when assigned
					put("ships", "8_65");         // Ships of Great Draught: Shadow possession in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CleavingBlowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Cleaving Blow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Skirmish: Make a Dwarf strength +2 and <b>damage +1</b>. Discard a Shadow item borne by
		 * a minion that Dwarf is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blow");

		assertEquals("Cleaving Blow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfMadeStrengthPlus2AndDamagePlus1WinsHisSkirmishAndWoundsTheMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		int twilight = scn.GetTwilight();
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertInZone(Zone.HAND, blow);
		assertTrue(scn.FreepsPlayAvailable(blow));

		// The Guard is the only Dwarf and the Savage bears nothing: no choice is asked.
		scn.FreepsPlayCard(blow);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, blow);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();

		// 6 against 5: the Guard wins, where at 4 he would have lost. 1 wound for losing + 1 for damage +1.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(savage));

		// The skirmish is over and so are the bonuses.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
	}

	@Test
	public void CanBePlayedInASkirmishAndNotInTheAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blow));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(blow));

		// Gimli, 6 with damage +1, beats the Savage, who survives with 2 wounds of 3.
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, blow);
		assertFalse(scn.FreepsPlayAvailable(blow));
	}

	@Test
	public void ADwarfWaitingForHisOwnSkirmishIsStrengthenedOnlyForThisSkirmishAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var blade1 = scn.GetShadowCard("blade1");
		var blade2 = scn.GetShadowCard("blade2");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// A Man of the [dwarven] culture is still no Dwarf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.AttachCardsTo(soldier1, blade1);
		scn.AttachCardsTo(soldier2, blade2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(guard, soldier1));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharAssignedAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsPlayCard(blow);
		// The Dwarf in the skirmish, the Dwarf waiting for his own, and the Dwarf ally in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// Gimli's own damage +1 and the event's add up.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(grimir));
		assertFalse(scn.HasKeyword(grimir, Keyword.DAMAGE));

		// Gimli is skirmishing nobody yet: not the Soldier the Guard is fighting, and not the Soldier assigned
		// to Gimli, whose skirmish has not begun. Neither Blade is discarded and no choice is asked.
		assertAttachedTo(blade1, soldier1);
		assertAttachedTo(blade2, soldier2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Guard, 4, loses to the Soldier, 7.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);

		// A new skirmish phase: the bonuses went with the last one.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertAttachedTo(blade2, soldier2);
	}

	@Test
	public void DiscardsOneShadowPossessionOrArtifactBorneByAMinionThatDwarfIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var lemenya = scn.GetShadowCard("lemenya");
		var blade1 = scn.GetShadowCard("blade1");
		var blade2 = scn.GetShadowCard("blade2");
		var rancor = scn.GetShadowCard("rancor");
		var enraged = scn.GetShadowCard("enraged");
		var poison = scn.GetShadowCard("poison");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(guard, gimli);
		// A Shadow possession borne by the Dwarf himself and one borne by nobody.
		scn.AttachCardsTo(gimli, poison);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2, lemenya);
		// Gimli will fight a Soldier with a possession and a condition, and a Nazgul with an artifact.
		scn.AttachCardsTo(soldier1, blade1, enraged);
		scn.AttachCardsTo(lemenya, rancor);
		// The Guard's Soldier, whose skirmish has not begun, has a possession too.
		scn.AttachCardsTo(soldier2, blade2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, soldier1}, new PhysicalCardImpl[]{guard, soldier2});
		scn.ShadowAssignToMinions(gimli, lemenya);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier1));
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, lemenya));
		assertTrue(scn.IsCharAssignedAgainst(guard, soldier2));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(blow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		scn.FreepsChooseCard(gimli);

		assertTrue(scn.FreepsHasCardChoiceAvailable(blade1, rancor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(enraged, blade2, poison, ships));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(rancor);
		assertInZone(Zone.DISCARD, rancor);
		assertAttachedTo(blade1, soldier1);
		assertAttachedTo(enraged, soldier1);
		assertAttachedTo(blade2, soldier2);
		assertAttachedTo(poison, gimli);
		assertInZone(Zone.SUPPORT, ships);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOnlyShadowItemOnTheMinionThatDwarfIsSkirmishingIsDiscardedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var blade1 = scn.GetShadowCard("blade1");
		var blade2 = scn.GetShadowCard("blade2");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.AttachCardsTo(soldier1, blade1);
		// The second Soldier is left unassigned and fights nobody.
		scn.AttachCardsTo(soldier2, blade2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, soldier1);

		assertAttachedTo(blade1, soldier1);
		assertEquals(0, scn.GetShadowDiscardCount());

		// Gimli is the only Dwarf and the first Soldier's Blade the only item he could discard.
		scn.FreepsPlayCard(blow);
		assertInZone(Zone.DISCARD, blade1);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertAttachedTo(blade2, soldier2);
		assertInZone(Zone.SUPPORT, ships);
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MayBePlayedWithNoDwarfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier1 = scn.GetShadowCard("soldier1");
		var blade1 = scn.GetShadowCard("blade1");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1);
		scn.AttachCardsTo(soldier1, blade1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier1);

		int twilight = scn.GetTwilight();
		assertEquals(8, scn.GetStrength(aragorn));
		// Nothing in the text is a cost or a requirement, so the event can be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(blow));

		scn.FreepsPlayCard(blow);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, blow);
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertAttachedTo(blade1, soldier1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
