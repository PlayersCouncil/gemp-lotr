package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hurl", "15_179");
					put("tracking", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; hunter 1 is its whole text
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard, no hunter, strength 8; his skirmish ability is used only on purpose
					put("cirith", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 is its whole text
					put("shingle", "12_145");    // Shingle in a Storm: an [uruk-hai] condition; its damage bonus wants 6 companions, its ability is used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, no hunter; his ability is not used
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6; his ability is not used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ViolentHurlStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Violent Hurl
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [uruk-hai] minion strength +2 (and <b>damage +1</b> if that minion is a hunter).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hurl");

		assertEquals("Violent Hurl", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHunterChosenIsStrengthPlus2AndDamagePlus1ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurl = scn.GetShadowCard("hurl");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		var cirith = scn.GetShadowCard("cirith");
		var shingle = scn.GetShadowCard("shingle");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(hurl);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, saruman, cirith);
		scn.MoveCardsToSupportArea(shingle);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tracking}, new PhysicalCardImpl[]{gimli, cirith});
		// Saruman is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 14 + 1 for hunter against a companion who is no hunter.
		assertEquals(15, scn.GetStrength(tracking));
		assertEquals(0, scn.GetKeywordCount(tracking, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(cirith));
		assertTrue(scn.ShadowPlayAvailable(hurl));
		scn.ShadowPlayCard(hurl);

		// Any [uruk-hai] minion, in this skirmish or not; no [sauron] Uruk-hai and no [uruk-hai] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cirith, shingle));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tracking);

		assertInZone(Zone.DISCARD, hurl);
		assertEquals(17, scn.GetStrength(tracking));
		assertEquals(1, scn.GetKeywordCount(tracking, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(cirith));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 17 against 8: Aragorn is overwhelmed, and the Tracking Uruk lives on into the next skirmish.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);
		scn.FreepsResolveSkirmish(gimli);

		// Each skirmish is a phase of its own: the bonus and the damage are gone, and out of its skirmish the
		// Tracking Uruk is its printed 14.
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(0, scn.GetKeywordCount(tracking, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatIsNoHunterIsStrengthPlus2AndGainsNoDamage() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurl = scn.GetShadowCard("hurl");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hurl);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		// Saruman is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(15, scn.GetStrength(tracking));
		assertEquals(0, scn.GetKeywordCount(tracking, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		scn.ShadowPlayCard(hurl);
		scn.ShadowChooseCard(saruman);

		// The chosen minion is no hunter: +2 alone, though a hunter is in play beside it.
		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(tracking));
		assertEquals(0, scn.GetKeywordCount(tracking, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhaseAndCanInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurl = scn.GetShadowCard("hurl");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hurl);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hurl));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hurl));
	}

	@Test
	public void WithNoUrukHaiCultureMinionItIsStillPlayedAndChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hurl = scn.GetShadowCard("hurl");
		var cirith = scn.GetShadowCard("cirith");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(hurl);

		scn.StartGame();
		scn.MoveMinionsToTable(cirith);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, cirith);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(cirith));
		assertEquals(1, scn.GetKeywordCount(cirith, Keyword.DAMAGE));
		// An event whose target finds nothing is still played (CR 4.2 "effect"; F-0665).
		assertTrue(scn.ShadowPlayAvailable(hurl));
		scn.ShadowPlayCard(hurl);

		assertInZone(Zone.DISCARD, hurl);
		assertEquals(9, scn.GetStrength(cirith));
		assertEquals(1, scn.GetKeywordCount(cirith, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
