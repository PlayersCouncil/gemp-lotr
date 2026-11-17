package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hammer", "11_130");
					put("hammer2", "11_130");

					// Bearers.  Scurrying Goblin: an [orc] Orc, vitality 1; its "may add (2)" wants an underground
					// site.  Denizen of Khazad-dûm: an [orc] Orc, vitality 2; stronger at an underground site only.
					put("goblin", "11_137");
					put("goblin2", "11_137");
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger is
					// never reached.
					put("halftroll", "14_12");
					// Morgul Slayer: an Orc of the [sauron] culture; a regroup ability only
					put("slayer", "3_93");
					// Orkish Ax: an [orc] hand weapon, "Bearer must be an [orc] Orc"; its trigger answers a skirmish won
					put("ax", "18_84");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: Man ally, a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcHammerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Orc Hammer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [orc] Orc.<br>When you play this possession, you may exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hammer");

		assertEquals("Orc Hammer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsBorneByAnOrcCultureOrcAndItsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetShadowCard("hammer");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var slayer = scn.GetShadowCard("slayer");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hammer);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen, slayer);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, denizen, slayer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hammer));
		scn.ShadowPlayCard(hammer);

		// [orc] Orcs of two titles; not an Orc of the [sauron] culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(slayer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertSame(denizen, hammer.getAttachedTo());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hammer));
		scn.ShadowDeclineOptionalTrigger();

		// Declined: nobody exerts.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertSame(denizen, hammer.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcCultureMinionWhoIsNoOrcIsNotOfferedAsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetShadowCard("hammer");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(hammer);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, denizen, halftroll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(hammer);

		// The Half-troll is of the [orc] culture and no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll));
		scn.ShadowChooseCard(goblin);

		assertSame(goblin, hammer.getAttachedTo());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hammer));
	}

	@Test
	public void WhenPlayedItMayExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetShadowCard("hammer");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToHand(hammer);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin, denizen);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(hammer);
		scn.ShadowChooseCard(goblin);
		assertSame(goblin, hammer.getAttachedTo());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hammer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.ShadowAcceptOptionalTrigger();

		// The Ring-bearer and a companion of another culture and race; not the exhausted Gimli, the ally or a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, albert, denizen));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetShadowCard("hammer");
		var goblin = scn.GetShadowCard("goblin");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToHand(hammer);
		// Vitality 4 with 3 wounds each: both exhausted.  The ally could exert, and is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(albert));

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(hammer);

		assertSame(goblin, hammer.getAttachedTo());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hammer));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcPossessionPlayedMakesNoOfferAndASecondHammersOfferIsItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetShadowCard("hammer");
		var hammer2 = scn.GetShadowCard("hammer2");
		var ax = scn.GetShadowCard("ax");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var goblin2 = scn.GetShadowCard("goblin2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hammer2, ax);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen, goblin2);
		scn.AttachCardsTo(goblin, hammer);
		assertSame(goblin, hammer.getAttachedTo());
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Another [orc] hand weapon is played while a Hammer is in play.
		scn.ShadowPlayCard(ax);
		scn.ShadowChooseCard(denizen);
		assertSame(denizen, ax.getAttachedTo());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Hammer is played on the second Goblin, the one Orc left with no hand weapon: the copy played
		// makes the offer and the copy in play makes none.
		scn.ShadowPlayCard(hammer2);
		assertSame(goblin2, hammer2.getAttachedTo());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hammer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hammer));
		scn.ShadowChooseOptionalTrigger(hammer2);
		scn.ShadowChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
