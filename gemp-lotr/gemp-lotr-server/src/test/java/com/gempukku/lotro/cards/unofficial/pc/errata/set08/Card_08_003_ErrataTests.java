package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_003_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brc", "58_3");

					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion, damage +1; a skirmish ability only
					put("axe", "1_14");        // Gimli's Battle Axe: bearer must be Gimli, he is damage +1; an optional trigger on winning a skirmish
					put("thrarin", "1_27");    // Thrarin, Dwarven Smith: Dwarf ally, no damage of his own; a maneuver ability only
					put("guard", "1_7");       // Dwarf Guard: Dwarf companion, no damage bonus
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man companion, no Dwarf; a maneuver ability used only on purpose

					put("troop", "1_177");     // Goblin Patrol Troop: [moria] minion, no text
					put("pinned", "1_192");    // Pinned Down: [moria] condition in the support area; nothing without a [moria] archer
					put("pinned2", "1_192");
					put("poison", "8_28");     // Spider Poison: Shadow possession; its bearer exerts when assigned, and a skirmish ability
					put("watcher1", "103_92"); // Cirith Ungol Watcher: response to a companion exerted by a Free Peoples card, hinders him
					put("watcher2", "103_92");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodRunsChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 8
		 * Name: Blood Runs Chill
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves, spot a Dwarf who is damage +X and exert that Dwarf to make an opponent
		 * hinder X Shadow cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brc");

		assertEquals("Blood Runs Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADamageDwarfToMakeShadowHinderThatManyCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var thrarin = scn.GetFreepsCard("thrarin");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveAlliesToTable(thrarin);
		// Thrarin is a Dwarf ally who is damage +1.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, thrarin, null, Keyword.DAMAGE, 1));
		// Aragorn is a Man who is damage +1, and no Dwarf.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.DAMAGE, 1));
		scn.MoveCardsToHand(brc);

		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		var pinned2 = scn.GetShadowCard("pinned2");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(pinned, pinned2);
		scn.HinderCard(pinned2);
		scn.AttachCardsTo(guard, poison);

		// Gimli is damage +1 printed, +1 from his axe.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// Not a fellowship event: nothing to play before the fellowship moves.
		assertFalse(scn.FreepsPlayAvailable(brc));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brc));
		assertInHand(brc);

		scn.FreepsChooseOptionalTrigger(brc);

		// Only Dwarves with a damage bonus can be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// Gimli is damage +2, so the Shadow player hinders 2 Shadow cards, of his choosing.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, pinned, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned2, axe, gimli));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, poison);

		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(axe));
		assertInDiscard(brc);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheRegroupMoveAnAllyWhoIsDamagePlusOneMakesShadowHinderOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		// Gimli is damage +2 with his axe, more than Thrarin, so X is shown to be the chosen Dwarf's and not the greatest.
		scn.AttachCardsTo(gimli, axe);
		scn.MoveAlliesToTable(thrarin);
		// Thrarin is a Dwarf ally who is damage +1.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, thrarin, null, Keyword.DAMAGE, 1));

		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(pinned);
		scn.MoveCardsToHand(brc);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brc));
		scn.FreepsChooseOptionalTrigger(brc);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, thrarin));
		scn.FreepsChooseCard(thrarin);

		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Thrarin is damage +1: one Shadow card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, pinned));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(pinned);

		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithAnExhaustedDwarfAndAManWhoIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		// Aragorn is a Man who is damage +1, and no Dwarf.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.DAMAGE, 1));
		scn.MoveCardsToHand(brc);

		var pinned = scn.GetShadowCard("pinned");

		scn.StartGame();
		scn.MoveCardsToSupportArea(pinned);
		// Vitality 3 with 2 wounds: Gimli is exhausted.
		scn.AddWoundsToChar(gimli, 2);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brc));
		assertInHand(brc);
		assertFalse(scn.IsHindered(pinned));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DamageBonusIsLockedInWhenTheDwarfIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1026: X was evaluated when the effect resolved. A Cirith Ungol Watcher responding to the
		// exertion hindered Gimli first, dropping his bonus to the printed +1, so Shadow only had to hinder 1 card.
		var scn = GetScenario();

		var brc = scn.GetFreepsCard("brc");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(brc);

		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		var pinned2 = scn.GetShadowCard("pinned2");
		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(pinned, pinned2, watcher1, watcher2);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brc));
		// Gimli is the only Dwarf: chosen and exerted without asking.
		scn.FreepsChooseOptionalTrigger(brc);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Watcher 1 answers the exertion: it discards the other Watcher and hinders Gimli.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watcher1));
		scn.ShadowChooseOptionalTrigger(watcher1);
		assertInDiscard(watcher2);
		assertTrue(scn.IsHindered(gimli));

		// X was fixed at +2 when the Dwarf was spotted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, pinned, pinned2, watcher1));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, pinned);

		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(pinned2));
		assertFalse(scn.IsHindered(watcher1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
