package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sharku", "5_58");
					put("warwarg", "5_64");     // War-warg: [isengard] mount on a warg-rider, strength +4, vitality +2; a skirmish Response
					put("hound", "102_24");     // Hound of Isengard: [isengard] mount on a warg-rider, vitality +1; its "when you play"
					                            // take needs an [isengard] skirmish event in the discard pile
					put("rider", "5_67");       // Warg-rider: warg-rider, strength 5, vitality 2; fierce while not exhausted
					put("axe", "3_55");         // Isengard Axe: an [isengard] possession that is no mount, on an [isengard] Orc; adds (1) when played
					put("viciouswarg", "17_92");// Vicious Warg: an [orc] mount, on an [orc] Orc of strength 9 or less; bearer is fierce
					put("denizen", "11_116");   // Denizen of Moria: [orc] Orc, strength 7 (+3 underground only)
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a Maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a Skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a Skirmish ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, vitality 3; an Archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SharkuStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sharku, Warg-captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While Sharku is not exhausted, he is <b>fierce</b>.
		 * Each time you play an [isengard] mount, you may make the Free Peoples player exert a companion (except a Hobbit).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sharku");

		assertEquals("Sharku", card.getBlueprint().getTitle());
		assertEquals("Warg-captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachIsengardMountPlayedLetsHimMakeTheFreePeoplesPlayerExertACompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");
		var warwarg = scn.GetShadowCard("warwarg");
		var hound = scn.GetShadowCard("hound");
		var rider = scn.GetShadowCard("rider");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn, gimli, pippin);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(warwarg, hound);
		assertInHand(warwarg, hound);

		scn.StartGame();
		scn.SetTwilight(6);
		// Laid before the Shadow phase begins, so that its first choice of actions takes them in.
		scn.MoveMinionsToTable(sharku, rider);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The War-warg goes on the Warg-rider, not on Sharku.
		scn.ShadowPlayCard(warwarg);
		scn.ShadowChooseCard(rider);
		assertAttachedTo(warwarg, rider);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sharku));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player chooses: a companion who is no Hobbit, whatever his culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, pippin, orophin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second [isengard] mount, on Sharku himself (the Warg-rider bears one already): a second exertion.
		scn.ShadowPlayCard(hound);
		assertAttachedTo(hound, sharku);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sharku));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedForHisOwnPlayNorAnIsengardPossessionThatIsNoMountNorAMountOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");
		var axe = scn.GetShadowCard("axe");
		var viciouswarg = scn.GetShadowCard("viciouswarg");
		var rider = scn.GetShadowCard("rider");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sharku, axe, viciouswarg);
		assertInHand(sharku, axe, viciouswarg);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.MoveMinionsToTable(rider, denizen);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Sharku's own play: an [isengard] card, a warg-rider, and no mount.
		scn.ShadowPlayCard(sharku);
		assertInZone(Zone.SHADOW_CHARACTERS, sharku);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sharku));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(axe);
		scn.ShadowChooseCard(rider);
		assertAttachedTo(axe, rider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sharku));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Denizen is the one bearer it can have.
		scn.ShadowPlayCard(viciouswarg);
		assertAttachedTo(viciouswarg, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sharku));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void IsNotOfferedWhenNoCompanionButAHobbitCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");
		var warwarg = scn.GetShadowCard("warwarg");
		var rider = scn.GetShadowCard("rider");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToHand(warwarg);
		assertInHand(warwarg);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  Frodo and Pippin are Hobbits.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SetTwilight(4);
		scn.MoveMinionsToTable(sharku, rider);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPlayCard(warwarg);
		scn.ShadowChooseCard(rider);
		assertAttachedTo(warwarg, rider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sharku));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void ExhaustedHeIsNotFierceUntilAWarWargPlayedOnHimAndTheExertionMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharku = scn.GetShadowCard("sharku");
		var warwarg = scn.GetShadowCard("warwarg");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warwarg);
		assertInHand(warwarg);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.MoveMinionsToTable(sharku, troop);
		// Unwounded he has vitality 2, one above exhausted, and is fierce.
		assertEquals(0, scn.GetWoundsOn(sharku));
		assertEquals(2, scn.GetVitality(sharku));
		assertFalse(scn.IsExhausted(sharku));
		assertTrue(scn.HasKeyword(sharku, Keyword.FIERCE));
		scn.AddWoundsToChar(sharku, 1);
		assertEquals(1, scn.GetWoundsOn(sharku));
		scn.FreepsPass();

		// Vitality 2 with 1 wound: exhausted, and not fierce; the Troop beside him is not fierce either.
		assertEquals(1, scn.GetWoundsOn(sharku));
		assertTrue(scn.IsExhausted(sharku));
		assertFalse(scn.HasKeyword(sharku, Keyword.FIERCE));
		assertFalse(scn.IsExhausted(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// He is the one warg-rider on the table.
		scn.ShadowPlayCard(warwarg);
		assertAttachedTo(warwarg, sharku);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sharku));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 2 + 2 less 1 wound: 3 left, not exhausted, fierce.
		assertEquals(1, scn.GetWoundsOn(sharku));
		assertEquals(3, scn.GetVitality(sharku));
		assertTrue(scn.HasKeyword(sharku, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
