package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "15_74");
					// Elder of Dunland: [men] Man, strength 10, vitality 3, site 4; damage +1 only at a plains site.
					put("elder", "11_77");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, site 2; a maneuver and a regroup
					// ability, each used only on purpose.
					put("ted", "18_75");
					put("brigand", "4_10");      // Dunlending Brigand: [dunland] Man minion, site 3, no text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChieftainofDunlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Chieftain of Dunland
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Fierce</b>.
		 * 		<b>Skirmish:</b> Spot a site you control and remove (2) to make a [men] Man strength +2.
		 * 		<b>Skirmish:</b> Spot 2 sites you control and remove (2) to make a [men] Man strength +3.
		 * 		<b>Skirmish:</b> Spot 3 sites you control and remove (2) to make a [men] Man strength +4.
		*/

		var scn = GetScenario();

		var chieftain = scn.GetFreepsCard("chieftain");

		assertEquals("Chieftain of Dunland", chieftain.getBlueprint().getTitle());
		assertNull(chieftain.getBlueprint().getSubtitle());
		assertTrue(chieftain.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, chieftain.getBlueprint().getSide());
		assertEquals(Culture.MEN, chieftain.getBlueprint().getCulture());
		assertEquals(CardType.MINION, chieftain.getBlueprint().getCardType());
		assertEquals(Race.MAN, chieftain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertEquals(5, chieftain.getBlueprint().getTwilightCost());
		assertEquals(11, chieftain.getBlueprint().getStrength());
		assertEquals(3, chieftain.getBlueprint().getVitality());
		assertEquals(4, chieftain.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteYouControlItRemovesTwoToMakeAMenManStrengthPlusTwoForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Both markers at site 4, so sites 1 to 3 may be taken, lowest first.
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		// Controlled by the Free Peoples player, as he might have taken it on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		// To The Bridge of Khazad-dum, site 5.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain, elder, ted, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gimli, elder});
		// Ted and the Brigand are left over, and the Shadow player assigns neither.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(chieftain, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +4"));

		scn.ShadowUseCardAction(chieftain, "strength +2");
		assertEquals(2, scn.GetTwilight());
		// The Chieftain in the skirmish and a [men] Man in none are offered; a [men] minion who is no Man, and a Man
		// of another culture, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, elder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(10, scn.GetStrength(elder));
		scn.ShadowChooseCard(elder);
		assertEquals(12, scn.GetStrength(elder));
		assertEquals(11, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish is over; in the Elder's own, the next skirmish phase, the bonus is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, elder));
		assertEquals(10, scn.GetStrength(elder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

	}

	@Test
	public void WithTwoSitesYouControlItRemovesTwoToMakeAMenManStrengthPlusThreeForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Both markers at site 4, so sites 1 to 3 may be taken, lowest first.
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		// Controlled by the Free Peoples player, as he might have taken it on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		// To The Bridge of Khazad-dum, site 5.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain, elder, ted, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gimli, elder});
		// Ted and the Brigand are left over, and the Shadow player assigns neither.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(chieftain, "strength +2"));
		assertTrue(scn.ShadowActionAvailable(chieftain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +4"));

		scn.ShadowUseCardAction(chieftain, "strength +3");
		assertEquals(2, scn.GetTwilight());
		// The Chieftain in the skirmish and a [men] Man in none are offered; a [men] minion who is no Man, and a Man
		// of another culture, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, elder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(10, scn.GetStrength(elder));
		scn.ShadowChooseCard(elder);
		assertEquals(13, scn.GetStrength(elder));
		assertEquals(11, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish is over; in the Elder's own, the next skirmish phase, the bonus is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, elder));
		assertEquals(10, scn.GetStrength(elder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

	}

	@Test
	public void WithThreeSitesYouControlItRemovesTwoToMakeAMenManStrengthPlusFourForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Both markers at site 4, so sites 1 to 3 may be taken, lowest first.
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(3, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		// To The Bridge of Khazad-dum, site 5.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain, elder, ted, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gimli, elder});
		// Ted and the Brigand are left over, and the Shadow player assigns neither.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(chieftain, "strength +2"));
		assertTrue(scn.ShadowActionAvailable(chieftain, "strength +3"));
		assertTrue(scn.ShadowActionAvailable(chieftain, "strength +4"));

		scn.ShadowUseCardAction(chieftain, "strength +4");
		assertEquals(2, scn.GetTwilight());
		// The Chieftain in the skirmish and a [men] Man in none are offered; a [men] minion who is no Man, and a Man
		// of another culture, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, elder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(10, scn.GetStrength(elder));
		scn.ShadowChooseCard(elder);
		assertEquals(14, scn.GetStrength(elder));
		assertEquals(11, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish is over; in the Elder's own, the next skirmish phase, the bonus is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, elder));
		assertEquals(10, scn.GetStrength(elder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

	}

	@Test
	public void NoAbilityIsOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Both markers at site 4, so sites 1 to 3 may be taken, lowest first.
		scn.SkipToSite(4);
		// Controlled by the Free Peoples player, as he might have taken it on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		// To The Bridge of Khazad-dum, site 5.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain, elder, ted, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gimli, elder});
		// Ted and the Brigand are left over, and the Shadow player assigns neither.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(4, scn.GetTwilight());
		// The only site controlled is the other player's.
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +4"));
		assertEquals(11, scn.GetStrength(chieftain));

	}

	@Test
	public void NoAbilityIsOfferedWithOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var chieftain = scn.GetShadowCard("chieftain");
		var elder = scn.GetShadowCard("elder");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Both markers at site 4, so sites 1 to 3 may be taken, lowest first.
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(3, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		// To The Bridge of Khazad-dum, site 5.
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain, elder, ted, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gimli, elder});
		// Ted and the Brigand are left over, and the Shadow player assigns neither.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(1);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetTwilight());
		// One short of the (2) each ability removes.
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(chieftain, "strength +4"));
		assertEquals(11, scn.GetStrength(chieftain));

	}
}
