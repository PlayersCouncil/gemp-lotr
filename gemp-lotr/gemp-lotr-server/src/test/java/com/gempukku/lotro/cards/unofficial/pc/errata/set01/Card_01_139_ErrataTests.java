package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_139_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savagery", "51_139");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, strength 3, vitality 4
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally, so a character who is no companion; her text needs your site 3

					put("uruk", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("uruk2", "1_151");      // a second Uruk Savage, left unassigned
			put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc that is no Uruk-hai, strength 6; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavagerytoMatchTheirNumbersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Savagery to Match Their Numbers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength +2.
		 * If you can spot 5 companions, also make it <b>fierce</b> and an additional strength +2 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("savagery");

		assertEquals("Savagery to Match Their Numbers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedInASkirmishItMakesAnUrukHaiStrengthPlus2ForThatSkirmishOnlyAndWithFourCompanionsNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savagery = scn.GetShadowCard("savagery");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var silinde = scn.GetFreepsCard("silinde");
		var uruk = scn.GetShadowCard("uruk");
		var sentry = scn.GetShadowCard("sentry");
		// Frodo and three more: four companions, and an ally beside them who is a character but no companion.
		scn.MoveCompanionsToTable(aragorn, sam, guard1);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, sentry);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(savagery));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(savagery));
		assertEquals(5, scn.GetStrength(uruk));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.ShadowPlayCard(savagery);
		scn.ShadowChooseCard(uruk);
		assertEquals(7, scn.GetStrength(uruk));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(sentry));
		assertInZone(Zone.DISCARD, savagery);

		// Aragorn, 8, beats the Savage, 7.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(uruk));

		// The next skirmish is another skirmish phase: the +2 has gone.
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(5, scn.GetStrength(uruk));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));

		// No fierce minion survives the skirmishes, so there is no fierce assignment.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(uruk));
	}

	@Test
	public void AnUrukHaiOfAnyCultureInTheSkirmishOrNotMayBeChosenAndAnOrcMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savagery = scn.GetShadowCard("savagery");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn, sam, guard1);
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, uruk2, sentry, forger);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry}, new PhysicalCardImpl[]{sam, forger});
		// The Shadow player may assign the second Savage to Frodo; he does not.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(savagery);
		// The Sentry is assigned to a skirmish that has not begun; the second Savage is assigned to nothing.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, sentry, uruk2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(sentry);
		assertEquals(10, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(uruk));
		assertEquals(6, scn.GetStrength(forger));

		// A skirmish action lasts for the skirmish it is used in, on a minion outside it too.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsThatUrukHaiIsAlsoFierceAndStrengthPlus2MoreUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savagery = scn.GetShadowCard("savagery");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var uruk = scn.GetShadowCard("uruk");
		var sentry = scn.GetShadowCard("sentry");
		// Frodo and four more: exactly five companions.
		scn.MoveCompanionsToTable(aragorn, sam, guard1, guard2);
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(uruk));

		scn.ShadowPlayCard(savagery);
		// No "may": the Shadow player is asked only which Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, sentry));
		scn.ShadowChooseCard(uruk);
		// 5 + 2 + an additional 2
		assertEquals(9, scn.GetStrength(uruk));
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(sentry));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, savagery);

		// The Savage, 9, beats Aragorn, 8: two wounds with damage +1.
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The next skirmish phase: the first +2 has gone, the additional +2 and fierce remain.
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(7, scn.GetStrength(uruk));
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, uruk);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(7, scn.GetStrength(uruk));
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.PassFierceSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(uruk));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
	}
}
