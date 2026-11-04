package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lords", "13_4");
					put("lords2", "13_4");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability only
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5 (+2 skirmishing an Orc)
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4; to play, spot a Dwarf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf, strength 8
					put("handaxe", "2_10");     // Hand Axe: [dwarven] hand weapon, strength +1; an archery ability only
					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon, strength +2; its trigger needs a skirmish won
					// Iron-forged Weapon: [esgaroth] hand weapon (a Hobbit-set card), strength +2, borne by a Man or a
					// [dwarven] companion; the only hand weapon of another culture a Dwarf can bear.  Its skirmish
					// ability is used only on purpose.
					put("ironforged", "32_18");
					put("ironforged2", "32_18");
					put("armor", "1_8");        // Dwarven Armor: [dwarven] armor a Dwarf bears, no hand weapon
					// [dwarven] cards of five kinds for the discard pile
					put("guard2", "1_7");       // a companion
					put("axe2", "1_9");         // a possession
					put("guile", "9_8");        // Ring of Guile: an artifact
					put("heart", "1_10");       // Dwarven Heart: a condition
					put("strike", "1_3");       // Axe Strike: an event
					// A card of another culture for the discard pile
					put("knight", "5_35");      // Gondorian Knight
					// A [dwarven] card of a title only the Shadow player's discard pile holds
					put("bracers", "2_3");      // Dwarven Bracers
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarflordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Dwarf-lords
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [dwarven] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make your Dwarf bearing a hand weapon strength +3.
		*/

		var scn = GetScenario();

		var lords = scn.GetFreepsCard("lords");

		assertEquals("Dwarf-lords", lords.getBlueprint().getTitle());
		assertNull(lords.getBlueprint().getSubtitle());
		assertFalse(lords.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, lords.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, lords.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, lords.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(lords, Timeword.SKIRMISH));
		assertEquals(2, lords.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherDwarvenCardsItMakesADwarfBearingAHandWeaponStrengthPlusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lords = scn.GetFreepsCard("lords");
		var lords2 = scn.GetFreepsCard("lords2");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var handaxe = scn.GetFreepsCard("handaxe");
		var axe = scn.GetFreepsCard("axe");
		var ironforged = scn.GetFreepsCard("ironforged");
		var ironforged2 = scn.GetFreepsCard("ironforged2");
		var armor = scn.GetFreepsCard("armor");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe2 = scn.GetFreepsCard("axe2");
		var guile = scn.GetFreepsCard("guile");
		var heart = scn.GetFreepsCard("heart");
		var strike = scn.GetFreepsCard("strike");
		var knight = scn.GetFreepsCard("knight");
		var bracers = scn.GetShadowCard("bracers");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, farin, guard, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, handaxe);
		scn.AttachCardsTo(farin, ironforged);
		scn.AttachCardsTo(grimir, axe);
		scn.AttachCardsTo(guard, armor);
		scn.AttachCardsTo(aragorn, ironforged2);
		scn.MoveCardsToDiscard(lords, guard2, axe2, guile, heart, strike, knight);
		scn.MoveCardsToDiscard(bracers);
		scn.MoveCardsToHand(lords2);

		scn.StartGame();
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ironforged, farin);
		assertAttachedTo(axe, grimir);
		assertAttachedTo(armor, guard);
		assertAttachedTo(ironforged2, aragorn);
		assertInZone(Zone.DISCARD, lords, guard2, axe2, guile, heart, strike, knight, bracers);
		assertInZone(Zone.HAND, lords2);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner1},
				new PhysicalCardImpl[]{farin, runner2});
		scn.FreepsResolveSkirmish(gimli);

		assertInZone(Zone.DISCARD, lords, guard2, axe2, guile, heart, strike, knight, bracers);
		assertInZone(Zone.HAND, lords2);
		// 6 + 1 for the Hand Axe.
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(lords));
		scn.FreepsUseCardAction(lords);
		// Five other [dwarven] cards, of five kinds, for the four.  Not the event itself, a card of another
		// culture, or the [dwarven] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, axe2, guile, heart, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lords, knight, bracers));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard2, axe2, guile, heart);
		assertInZone(Zone.REMOVED, guard2, axe2, guile, heart);
		assertInZone(Zone.DISCARD, strike, knight, bracers);

		// A Dwarf bearing a hand weapon: Gimli in this skirmish (a [dwarven] one), Farin assigned and waiting (one
		// of another culture), Grimir an ally.  Not the Dwarf Guard, who bears armor only, Aragorn, who bears a
		// hand weapon and is no Dwarf, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertEquals(10, scn.GetStrength(gimli));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, lords);
		assertInZone(Zone.HAND, lords2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: Gimli is 7 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(gimli));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherDwarvenCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lords = scn.GetFreepsCard("lords");
		var gimli = scn.GetFreepsCard("gimli");
		var handaxe = scn.GetFreepsCard("handaxe");
		var axe2 = scn.GetFreepsCard("axe2");
		var guile = scn.GetFreepsCard("guile");
		var heart = scn.GetFreepsCard("heart");
		var strike = scn.GetFreepsCard("strike");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, handaxe);
		// Four [dwarven] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(lords, axe2, guile, heart, knight);

		scn.StartGame();
		assertAttachedTo(handaxe, gimli);
		assertInZone(Zone.DISCARD, lords, axe2, guile, heart, knight);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner1);
		scn.FreepsResolveSkirmish(gimli);

		assertInZone(Zone.DISCARD, lords, axe2, guile, heart, knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(lords));

		// A fourth other [dwarven] card.
		scn.MoveCardsToDiscard(strike);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(lords));
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndFromHandWithNoDwarfBearingAHandWeaponItGoesToTheDiscardPileAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lords = scn.GetFreepsCard("lords");
		var lords2 = scn.GetFreepsCard("lords2");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var ironforged = scn.GetFreepsCard("ironforged");
		var axe2 = scn.GetFreepsCard("axe2");
		var guile = scn.GetFreepsCard("guile");
		var heart = scn.GetFreepsCard("heart");
		var strike = scn.GetFreepsCard("strike");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AttachCardsTo(guard, armor);
		scn.AttachCardsTo(aragorn, ironforged);
		scn.MoveCardsToHand(lords);
		// The second copy lies in the discard pile with four other [dwarven] cards.
		scn.MoveCardsToDiscard(lords2, axe2, guile, heart, strike);

		scn.StartGame();
		assertAttachedTo(armor, guard);
		assertAttachedTo(ironforged, aragorn);
		assertInZone(Zone.HAND, lords);
		assertInZone(Zone.DISCARD, lords2, axe2, guile, heart, strike);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		// A Skirmish event: neither copy at the assignment-phase decision.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lords));
		assertFalse(scn.FreepsActionAvailable(lords2));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(lords));
		assertTrue(scn.FreepsActionAvailable(lords2));
		// 8 + 2 for the weapon; 4 for the Guard.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		// No Dwarf bears a hand weapon: the event is played for nothing, and from hand it goes to the discard
		// pile with nothing removed from there.
		scn.FreepsPlayCard(lords);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, lords, lords2, axe2, guile, heart, strike);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
