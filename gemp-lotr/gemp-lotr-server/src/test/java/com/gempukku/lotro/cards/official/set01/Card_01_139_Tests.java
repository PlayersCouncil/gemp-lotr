package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savagery", "1_139");

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

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savagery", "1_139");

					// Aragorn, Ranger of the North: strength 8, vitality 4, so the Savage at 5 + 4 beats him
					// without overwhelming him
					put("aragorn", "1_89");
					// Companions of three more titles, to make five with the Ring-bearer.  None has a text that
					// touches a skirmish he is not in.
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("merry", "1_303");      // Merry, From O'er the Brandywine
					put("pippin", "1_306");     // Pippin, Friend to Frodo

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, Damage +1; the Uruk-hai in no skirmish
					// Isengard Worker: an [isengard] Orc, strength 5, with a regroup ability used only on purpose.
					// The culture without the race.
					put("worker", "3_62");
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
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength +2, or spot 5 companions to make an Uruk-hai strength +4 and <b>fierce</b> until the regroup phase.
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
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFiveCompanionsTheShadowPlayerChoosesBetweenStrengthPlus2AndStrengthPlus4WithFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		// With the Ring-bearer, five companions
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("sam"), scn.GetFreepsCard("merry"), scn.GetFreepsCard("pippin"));

		var savagery = scn.GetShadowCard("savagery");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(savagery));

		scn.ShadowPlayCard(savagery);
		// Both halves of the "or" can be done, so which is done is the Shadow player's to say.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("+2"));
		assertTrue(scn.ShadowChoiceAvailable("+4"));

		scn.ShadowChoose("+4");
		// An Uruk-hai, in the skirmish or out of it; the [isengard] Orc is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(savage);
		// 5 + 4
		assertEquals(9, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(raiders));
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertInDiscard(savagery);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFourCompanionsItMakesAnUrukHaiStrengthPlus2AndTheShadowPlayerIsNotAskedWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		// With the Ring-bearer, four companions: one short of the five the second half spots
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("sam"), scn.GetFreepsCard("merry"));

		var savagery = scn.GetShadowCard("savagery");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.ShadowPlayAvailable(savagery));

		scn.ShadowPlayCard(savagery);
		// "Spot 5 companions to ..." cannot be done, so there is nothing to choose between: the game goes
		// straight to which Uruk-hai.
		assertEquals(0, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));

		scn.ShadowChooseCard(savage);
		// 5 + 2
		assertEquals(7, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(raiders));
		assertInDiscard(savagery);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
	@Test
	public void PlayedInASkirmishWithFourCompanionsItMakesAnUrukHaiStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
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
		// Not a skirmish phase: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(savagery));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(savagery));
		assertEquals(5, scn.GetStrength(uruk));

		scn.ShadowPlayCard(savagery);
		// Five companions cannot be spotted, so the Shadow player is asked only which Uruk-hai.
		assertEquals(0, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseCard(uruk);
		// 5 + 2
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

		// No fierce minion survives the skirmishes, so there is no fierce assignment.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(uruk));
	}

	@Test
	public void WithFiveCompanionsThePlus2MayStillBeChosenForAnUrukHaiInTheSkirmishOrNotAndNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savagery = scn.GetShadowCard("savagery");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		// Frodo and four more: five companions.
		scn.MoveCompanionsToTable(aragorn, sam, guard1, guard2);
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, uruk2, sentry, forger);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{guard1, sentry}, new PhysicalCardImpl[]{sam, forger});
		// The Shadow player may assign the second Savage; he does not.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(sentry));
		scn.ShadowPlayCard(savagery);
		assertTrue(scn.ShadowChoiceAvailable("+2"));
		assertTrue(scn.ShadowChoiceAvailable("+4"));
		scn.ShadowChoose("+2");

		// The Sentry is assigned to a skirmish that has not begun; the second Savage is assigned to nothing.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, sentry, uruk2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(sentry);
		// 8 + 2, and not fierce
		assertEquals(10, scn.GetStrength(sentry));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertEquals(5, scn.GetStrength(uruk));
		assertEquals(6, scn.GetStrength(forger));

		// It lasts for the skirmish it is played in, on a minion outside it too.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsStrengthPlus4AndFierceLastUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.ShadowChoose("+4");
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, sentry));
		scn.ShadowChooseCard(uruk);
		// 5 + 4
		assertEquals(9, scn.GetStrength(uruk));
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(sentry));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, savagery);

		// The Savage, 9, beats Aragorn, 8: two wounds with damage +1.
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The next skirmish phase: the whole +4 and fierce remain (the Decipher ruling).
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(9, scn.GetStrength(uruk));
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.PassSkirmishActions();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, uruk);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(9, scn.GetStrength(uruk));
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));

		scn.PassFierceSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(uruk));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
	}
}
