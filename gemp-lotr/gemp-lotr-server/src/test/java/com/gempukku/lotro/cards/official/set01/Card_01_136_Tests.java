package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power", "1_136");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability only ever used on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, vitality 2; stronger only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion to bear the cards below
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] Free Peoples condition, support area
					put("saga", "1_114");       // The Saga of Elendil: [gondor] Free Peoples condition on a [gondor] companion
					put("arms", "7_252");       // Strong Arms: [rohan] Free Peoples condition that plays on a site
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, support area
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] Shadow condition, plays on Aragorn
					put("twilight", "19_34");   // In Twilight: [wraith] Shadow condition borne by a [wraith] minion
					put("spies", "1_140");      // Spies of Saruman: [isengard] Shadow condition that plays on a site; skips archery there
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion
					put("coat", "2_105");       // Mithril-coat: [shire] artifact borne by Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Power
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Spell</b>.<br><b>Shadow:</b> Exert an [isengard] minion to discard all conditions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("power");

		assertEquals("Saruman's Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToDiscardEveryActiveConditionAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var arms = scn.GetFreepsCard("arms");
		var coat = scn.GetFreepsCard("coat");
		var pinned = scn.GetShadowCard("pinned");
		var legacy = scn.GetShadowCard("legacy");
		var twilight = scn.GetShadowCard("twilight");
		var spies = scn.GetShadowCard("spies");
		var poison = scn.GetShadowCard("poison");
		// Inactive conditions on the Free Peoples player's turn: the Shadow player's own Free Peoples condition,
		// and the Free Peoples player's own Shadow condition.
		var shadowStairs = scn.GetShadowCard("stairs");
		var freepsPinned = scn.GetFreepsCard("pinned");

		scn.MoveCardsToHand(power);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs, pinned, shadowStairs, freepsPinned);
		scn.AttachCardsTo(aragorn, saga, legacy, poison);
		scn.AttachCardsTo(frodo, coat);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, servant, guard, troop, hound);
		scn.AttachCardsTo(hound, twilight);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		scn.AttachCardsTo(site, arms, spies);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		// [isengard] minions of two races; not a [sauron] Uruk-hai, a [moria] Orc or a [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);

		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(hound));
		// Conditions of both sides, in a support area, on a companion, on a minion and on a site.
		assertInDiscard(power, stairs, saga, arms, pinned, legacy, twilight, spies);
		// A Shadow possession and a Free Peoples artifact are no conditions.
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(coat, frodo);
		// Inactive conditions are not discarded.
		assertInZone(Zone.SUPPORT, shadowStairs, freepsPinned);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(power);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.AddWoundsToChar(savage, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: Uruk Savage can still exert.
		assertTrue(scn.ShadowPlayAvailable(power));

		scn.AddWoundsToChar(savage, 1);
		scn.ShadowRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted.  The [moria] minion beside it could exert.
		assertFalse(scn.ShadowPlayAvailable(power));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
